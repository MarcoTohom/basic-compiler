import java.util.List;

/**
 * FASE 2 - ANALISIS SINTACTICO (Parser de descenso recursivo)
 * FASE 3 - ANALISIS SEMANTICO  (integrado)
 * ============================================================
 *
 * Gramatica del lenguaje:
 *
 *   programa       -> instruccion* EOF
 *   instruccion    -> declaracion
 *                  |  asignacion
 *                  |  sentenciaIf
 *                  |  sentenciaWhile
 *                  |  sentenciaDo
 *                  |  sentenciaPrint
 *                  |  bloque
 *   declaracion    -> tipo IDENTIFIER ('=' expr)? ';'
 *   asignacion     -> IDENTIFIER '=' expr ';'
 *   sentenciaIf    -> 'if' '(' expr ')' bloque ('else' bloque)?
 *   sentenciaWhile -> 'while' '(' expr ')' bloque
 *   sentenciaDo    -> 'do' bloque 'while' '(' expr ')' ';'
 *   sentenciaPrint -> 'print' '(' expr ')' ';'
 *   bloque         -> '{' instruccion* '}'
 *   expr           -> comparacion
 *   comparacion    -> suma (('=='|'!='|'<'|'>'|'<='|'>=') suma)*
 *   suma           -> termino (('+'|'-') termino)*
 *   termino        -> factor (('*'|'/') factor)*
 *   factor         -> NUMBER | STRING_CONST | CHAR_CONST | IDENTIFIER | '(' expr ')'
 *   tipo           -> 'int' | 'string' | 'char'
 */
public class Parser {

    // Resultado de una expresion: su lugar (temporal/nombre) y tipo inferido
    private static class ExprResult {
        String lugar;
        String tipo;

        ExprResult(String lugar, String tipo) {
            this.lugar = lugar;
            this.tipo  = tipo;
        }
    }

    private final List<Token>           tokens;
    private final List<ErrorCompilador> errores;
    private final TablaSimbolos         tabla;
    private final GeneradorCodigo       gen;

    private int pos = 0;

    public Parser(List<Token> tokens,
                  List<ErrorCompilador> errores,
                  TablaSimbolos tabla,
                  GeneradorCodigo gen) {
        this.tokens  = tokens;
        this.errores = errores;
        this.tabla   = tabla;
        this.gen     = gen;
    }

    // -----------------------------------------------------------------------
    // ENTRADA: programa
    // -----------------------------------------------------------------------
    public void parsearPrograma() {
        while (!verificar(TokenType.EOF)) {
            try {
                parsearInstruccion();
            } catch (ErrorSintactico e) {
                sincronizar();
            }
        }
        gen.emitirHalt();
    }

    // -----------------------------------------------------------------------
    // instruccion
    // -----------------------------------------------------------------------
    private void parsearInstruccion() {
        if (esTipo()) {
            parsearDeclaracion();
        } else if (verificar(TokenType.IDENTIFIER)) {
            parsearAsignacion();
        } else if (verificar(TokenType.IF)) {
            parsearIf();
        } else if (verificar(TokenType.WHILE)) {
            parsearWhile();
        } else if (verificar(TokenType.DO)) {
            parsearDo();
        } else if (verificar(TokenType.PRINT)) {
            parsearPrint();
        } else if (verificar(TokenType.LBRACE)) {
            parsearBloque();
        } else {
            Token t = actual();
            errorSintactico(t, "Instruccion invalida: '" + t.lexema + "'");
            throw new ErrorSintactico();
        }
    }

    // -----------------------------------------------------------------------
    // declaracion -> tipo IDENTIFIER ('=' expr)? ';'
    // -----------------------------------------------------------------------
    private void parsearDeclaracion() {
        Token tokenTipo = actual();
        String tipo = tokenTipo.lexema;
        avanzar();

        Token tokenNombre = consumir(TokenType.IDENTIFIER,
                "Se esperaba un identificador despues de '" + tipo + "'");

        // Semantico: verificar redeclaracion
        boolean ok = tabla.declarar(tokenNombre.lexema, tipo, tokenNombre.linea);
        if (!ok) {
            errorSemantico(tokenNombre,
                    "Variable '" + tokenNombre.lexema + "' ya declarada en este ambito");
        }

        if (coincideYAvanza(TokenType.ASSIGN)) {
            ExprResult res = parsearExpr();
            verificarCompatibilidadTipos(tipo, res.tipo, tokenNombre);
            gen.emitirAsig(tokenNombre.lexema, res.lugar);
            tabla.setValor(tokenNombre.lexema, res.lugar);
        }

        consumir(TokenType.SEMICOLON, "Se esperaba ';' al final de la declaracion");
    }

    // -----------------------------------------------------------------------
    // asignacion -> IDENTIFIER '=' expr ';'
    // -----------------------------------------------------------------------
    private void parsearAsignacion() {
        Token tokenNombre = actual();
        avanzar();

        // Semantico: verificar que este declarada
        TablaSimbolos.Simbolo simbolo = tabla.buscar(tokenNombre.lexema);
        if (simbolo == null) {
            errorSemantico(tokenNombre,
                    "Variable '" + tokenNombre.lexema + "' no declarada");
        }

        consumir(TokenType.ASSIGN, "Se esperaba '=' en la asignacion");
        ExprResult res = parsearExpr();

        if (simbolo != null) {
            verificarCompatibilidadTipos(simbolo.tipo, res.tipo, tokenNombre);
            tabla.setValor(tokenNombre.lexema, res.lugar);
        }

        gen.emitirAsig(tokenNombre.lexema, res.lugar);
        consumir(TokenType.SEMICOLON, "Se esperaba ';' al final de la asignacion");
    }

    // -----------------------------------------------------------------------
    // sentenciaIf -> 'if' '(' expr ')' bloque ('else' bloque)?
    // -----------------------------------------------------------------------
    private void parsearIf() {
        consumir(TokenType.IF,     "Se esperaba 'if'");
        consumir(TokenType.LPAREN, "Se esperaba '(' despues de 'if'");
        ExprResult cond = parsearExpr();
        consumir(TokenType.RPAREN, "Se esperaba ')' para cerrar condicion del 'if'");

        String labelSi  = gen.nuevaLabel();
        String labelNo  = gen.nuevaLabel();
        String labelFin = gen.nuevaLabel();

        gen.emitirSaltoCond(cond.lugar, labelSi);
        gen.emitirSalto(labelNo);
        gen.emitirLabel(labelSi);

        parsearBloque();

        if (verificar(TokenType.ELSE)) {
            avanzar();
            gen.emitirSalto(labelFin);
            gen.emitirLabel(labelNo);
            parsearBloque();
            gen.emitirLabel(labelFin);
        } else {
            gen.emitirLabel(labelNo);
        }
    }

    // -----------------------------------------------------------------------
    // sentenciaWhile -> 'while' '(' expr ')' bloque
    // -----------------------------------------------------------------------
    private void parsearWhile() {
        consumir(TokenType.WHILE, "Se esperaba 'while'");

        String labelInicio = gen.nuevaLabel();
        String labelCuerpo = gen.nuevaLabel();
        String labelFin    = gen.nuevaLabel();

        gen.emitirLabel(labelInicio);
        consumir(TokenType.LPAREN, "Se esperaba '(' despues de 'while'");
        ExprResult cond = parsearExpr();
        consumir(TokenType.RPAREN, "Se esperaba ')' para cerrar condicion del 'while'");

        gen.emitirSaltoCond(cond.lugar, labelCuerpo);
        gen.emitirSalto(labelFin);
        gen.emitirLabel(labelCuerpo);

        parsearBloque();

        gen.emitirSalto(labelInicio);
        gen.emitirLabel(labelFin);
    }

    // -----------------------------------------------------------------------
    // sentenciaDo -> 'do' bloque 'while' '(' expr ')' ';'
    // -----------------------------------------------------------------------
    private void parsearDo() {
        consumir(TokenType.DO, "Se esperaba 'do'");

        String labelInicio = gen.nuevaLabel();
        String labelFin    = gen.nuevaLabel();

        gen.emitirLabel(labelInicio);
        parsearBloque();

        consumir(TokenType.WHILE,     "Se esperaba 'while' despues del bloque 'do'");
        consumir(TokenType.LPAREN,    "Se esperaba '(' despues de 'while' en 'do-while'");
        ExprResult cond = parsearExpr();
        consumir(TokenType.RPAREN,    "Se esperaba ')' para cerrar condicion del 'do-while'");
        consumir(TokenType.SEMICOLON, "Se esperaba ';' al final de 'do-while'");

        gen.emitirSaltoCond(cond.lugar, labelInicio);
        gen.emitirLabel(labelFin);
    }

    // -----------------------------------------------------------------------
    // sentenciaPrint -> 'print' '(' expr ')' ';'
    // -----------------------------------------------------------------------
    private void parsearPrint() {
        consumir(TokenType.PRINT,     "Se esperaba 'print'");
        consumir(TokenType.LPAREN,    "Se esperaba '(' despues de 'print'");
        ExprResult res = parsearExpr();
        consumir(TokenType.RPAREN,    "Se esperaba ')' para cerrar 'print'");
        consumir(TokenType.SEMICOLON, "Se esperaba ';' al final de 'print'");

        gen.emitirPrint(res.lugar);
    }

    // -----------------------------------------------------------------------
    // bloque -> '{' instruccion* '}'
    // -----------------------------------------------------------------------
    private void parsearBloque() {
        consumir(TokenType.LBRACE, "Se esperaba '{'");
        tabla.abrirAmbito();

        while (!verificar(TokenType.RBRACE) && !verificar(TokenType.EOF)) {
            try {
                parsearInstruccion();
            } catch (ErrorSintactico e) {
                sincronizar();
            }
        }

        tabla.cerrarAmbito();
        consumir(TokenType.RBRACE, "Se esperaba '}' para cerrar bloque");
    }

    // -----------------------------------------------------------------------
    // Expresiones
    // -----------------------------------------------------------------------
    private ExprResult parsearExpr() {
        return parsearComparacion();
    }

    private ExprResult parsearComparacion() {
        ExprResult izq = parsearSuma();
        while (esOperadorRelacional()) {
            Token op = actual(); avanzar();
            ExprResult der = parsearSuma();
            String tmp = gen.nuevoTemp();
            gen.emitirBinaria(tmp, izq.lugar, op.lexema, der.lugar);
            izq = new ExprResult(tmp, "bool");
        }
        return izq;
    }

    private ExprResult parsearSuma() {
        ExprResult izq = parsearTermino();
        while (verificar(TokenType.PLUS) || verificar(TokenType.MINUS)) {
            Token op = actual(); avanzar();
            ExprResult der = parsearTermino();
            if ("string".equals(izq.tipo) || "string".equals(der.tipo)) {
                if (op.tipo == TokenType.MINUS || op.tipo == TokenType.MULTIPLY) {
                    errorSemantico(op,
                            "Operacion '" + op.lexema + "' no permitida con tipo 'string'");
                }
            }
            String tmp = gen.nuevoTemp();
            gen.emitirBinaria(tmp, izq.lugar, op.lexema, der.lugar);
            izq = new ExprResult(tmp, izq.tipo);
        }
        return izq;
    }

    private ExprResult parsearTermino() {
        ExprResult izq = parsearFactor();
        while (verificar(TokenType.MULTIPLY) || verificar(TokenType.DIVIDE)) {
            Token op = actual(); avanzar();
            ExprResult der = parsearFactor();
            if (!"int".equals(izq.tipo) || !"int".equals(der.tipo)) {
                errorSemantico(op,
                        "Operacion '" + op.lexema + "' solo se permite entre enteros");
            }
            String tmp = gen.nuevoTemp();
            gen.emitirBinaria(tmp, izq.lugar, op.lexema, der.lugar);
            izq = new ExprResult(tmp, "int");
        }
        return izq;
    }

    private ExprResult parsearFactor() {
        Token t = actual();

        if (verificar(TokenType.NUMBER)) {
            avanzar();
            return new ExprResult(t.lexema, "int");
        }
        if (verificar(TokenType.STRING_CONST)) {
            avanzar();
            return new ExprResult(t.lexema, "string");
        }
        if (verificar(TokenType.CHAR_CONST)) {
            avanzar();
            return new ExprResult(t.lexema, "char");
        }
        if (verificar(TokenType.IDENTIFIER)) {
            avanzar();
            TablaSimbolos.Simbolo sim = tabla.buscar(t.lexema);
            if (sim == null) {
                errorSemantico(t, "Variable '" + t.lexema + "' no declarada");
                return new ExprResult(t.lexema, "int"); // recuperacion
            }
            if (!sim.inicializado) {
                errorSemantico(t,
                        "Variable '" + t.lexema + "' usada antes de inicializarse");
            }
            return new ExprResult(t.lexema, sim.tipo);
        }
        if (coincideYAvanza(TokenType.LPAREN)) {
            ExprResult res = parsearExpr();
            consumir(TokenType.RPAREN, "Se esperaba ')' al cerrar expresion");
            return res;
        }

        errorSintactico(t, "Expresion invalida: token inesperado '" + t.lexema + "'");
        throw new ErrorSintactico();
    }

    // -----------------------------------------------------------------------
    // Verificacion semantica de tipos
    // -----------------------------------------------------------------------
    private void verificarCompatibilidadTipos(String tipoDeclarado,
                                               String tipoAsignado,
                                               Token tokenNombre) {
        if (tipoAsignado == null || tipoAsignado.equals("bool")) return;
        if (!tipoDeclarado.equals(tipoAsignado)) {
            errorSemantico(tokenNombre,
                    "Incompatibilidad de tipos: variable '" + tokenNombre.lexema +
                    "' es '" + tipoDeclarado + "' pero se asigna '" + tipoAsignado + "'");
        }
    }

    // -----------------------------------------------------------------------
    // Utilidades de tokens
    // -----------------------------------------------------------------------
    private Token actual()  { return tokens.get(pos); }

    private Token avanzar() {
        Token t = tokens.get(pos);
        if (pos < tokens.size() - 1) pos++;
        return t;
    }

    private boolean verificar(TokenType tipo) {
        return actual().tipo == tipo;
    }

    private boolean coincideYAvanza(TokenType tipo) {
        if (!verificar(tipo)) return false;
        avanzar();
        return true;
    }

    private Token consumir(TokenType tipo, String mensajeError) {
        if (verificar(tipo)) return avanzar();
        Token t = actual();
        errorSintactico(t, mensajeError + " (encontrado: '" + t.lexema + "')");
        throw new ErrorSintactico();
    }

    private boolean esTipo() {
        TokenType t = actual().tipo;
        return t == TokenType.INT || t == TokenType.STRING || t == TokenType.CHAR;
    }

    private boolean esOperadorRelacional() {
        TokenType t = actual().tipo;
        return t == TokenType.EQ  || t == TokenType.NEQ ||
               t == TokenType.LT  || t == TokenType.GT  ||
               t == TokenType.LEQ || t == TokenType.GEQ;
    }

    // -----------------------------------------------------------------------
    // Manejo de errores y modo panico
    // -----------------------------------------------------------------------

    /** Excepcion de control interno para el modo panico */
    private static class ErrorSintactico extends RuntimeException {
        ErrorSintactico() { super(null, null, true, false); }
    }

    private void errorSintactico(Token t, String mensaje) {
        errores.add(new ErrorCompilador(
                ErrorCompilador.Fase.SINTACTICA, t.linea, t.columna, mensaje));
    }

    private void errorSemantico(Token t, String mensaje) {
        errores.add(new ErrorCompilador(
                ErrorCompilador.Fase.SEMANTICA, t.linea, t.columna, mensaje));
    }

    /**
     * Modo panico: descarta tokens hasta encontrar ';' o '}' y asi continuar
     * detectando mas errores en el resto del archivo.
     */
    private void sincronizar() {
        while (!verificar(TokenType.EOF)) {
            if (verificar(TokenType.SEMICOLON)) {
                avanzar();
                return;
            }
            if (verificar(TokenType.RBRACE)) {
                return;
            }
            TokenType t = actual().tipo;
            if (t == TokenType.IF    || t == TokenType.WHILE ||
                t == TokenType.DO    || t == TokenType.PRINT ||
                t == TokenType.INT   || t == TokenType.STRING ||
                t == TokenType.CHAR  || t == TokenType.LBRACE) {
                return;
            }
            avanzar();
        }
    }
}
