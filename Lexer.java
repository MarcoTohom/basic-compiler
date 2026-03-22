import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FASE 1 - ANALISIS LEXICO (Scanner / Lexer)
 * ===========================================
 * Lee el codigo fuente caracter a caracter y produce una lista de Tokens.
 * Los errores lexicos NO detienen el proceso: se acumulan en la lista global
 * y el analisis continua para detectar mas problemas.
 */
public class Lexer {

    // Palabras reservadas del lenguaje
    private static final Map<String, TokenType> PALABRAS_RESERVADAS = new HashMap<>();
    static {
        PALABRAS_RESERVADAS.put("if",     TokenType.IF);
        PALABRAS_RESERVADAS.put("else",   TokenType.ELSE);
        PALABRAS_RESERVADAS.put("while",  TokenType.WHILE);
        PALABRAS_RESERVADAS.put("do",     TokenType.DO);
        PALABRAS_RESERVADAS.put("int",    TokenType.INT);
        PALABRAS_RESERVADAS.put("string", TokenType.STRING);
        PALABRAS_RESERVADAS.put("char",   TokenType.CHAR);
        PALABRAS_RESERVADAS.put("print",  TokenType.PRINT);
    }

    private final String               fuente;
    private final List<Token>          tokens  = new ArrayList<>();
    private final List<ErrorCompilador> errores;

    private int inicio    = 0;
    private int actual    = 0;
    private int linea     = 1;
    private int columna   = 1;
    private int colInicio = 1;

    public Lexer(String fuente, List<ErrorCompilador> errores) {
        this.fuente  = fuente;
        this.errores = errores;
    }

    // API publica
    public List<Token> tokenizar() {
        while (!finDelFuente()) {
            inicio    = actual;
            colInicio = columna;
            escanearToken();
        }
        tokens.add(new Token(TokenType.EOF, "EOF", linea, columna));
        return tokens;
    }

    // Ciclo principal de escaneo
    private void escanearToken() {
        char c = avanzar();

        switch (c) {
            case '(': agregarToken(TokenType.LPAREN);    break;
            case ')': agregarToken(TokenType.RPAREN);    break;
            case '{': agregarToken(TokenType.LBRACE);    break;
            case '}': agregarToken(TokenType.RBRACE);    break;
            case ';': agregarToken(TokenType.SEMICOLON); break;
            case '+': agregarToken(TokenType.PLUS);      break;
            case '-': agregarToken(TokenType.MINUS);     break;
            case '*': agregarToken(TokenType.MULTIPLY);  break;

            case '/':
                if (coincide('/')) {
                    // comentario de linea: ignorar hasta fin de linea
                    while (vistazo() != '\n' && !finDelFuente()) avanzar();
                } else if (coincide('*')) {
                    // comentario de bloque
                    comentarioBloque();
                } else {
                    agregarToken(TokenType.DIVIDE);
                }
                break;

            case '=': agregarToken(coincide('=') ? TokenType.EQ  : TokenType.ASSIGN); break;
            case '!': agregarToken(coincide('=') ? TokenType.NEQ : TokenType.UNKNOWN); break;
            case '<': agregarToken(coincide('=') ? TokenType.LEQ : TokenType.LT);      break;
            case '>': agregarToken(coincide('=') ? TokenType.GEQ : TokenType.GT);      break;

            case ' ':
            case '\r':
            case '\t':
                break; // ignorar espacios

            case '\n':
                linea++;
                columna = 1;
                break;

            case '"':
                escanearCadena();
                break;

            case '\'':
                escanearChar();
                break;

            default:
                if (esDigito(c)) {
                    escanearNumero();
                } else if (esLetra(c)) {
                    escanearIdentificador();
                } else {
                    registrarError(linea, colInicio,
                            "Caracter invalido: '" + c + "'");
                }
        }
    }

    // Numeros: [0-9]+
    private void escanearNumero() {
        while (esDigito(vistazo())) avanzar();
        if (vistazo() == '.' && esDigito(vistazoDos())) {
            registrarError(linea, colInicio,
                    "Constante de punto flotante no soportada");
            avanzar();
            while (esDigito(vistazo())) avanzar();
        }
        agregarToken(TokenType.NUMBER);
    }

    // Cadenas: "..."
    private void escanearCadena() {
        while (vistazo() != '"' && !finDelFuente()) {
            if (vistazo() == '\n') { linea++; columna = 1; }
            avanzar();
        }
        if (finDelFuente()) {
            registrarError(linea, colInicio, "Cadena no cerrada");
            return;
        }
        avanzar(); // consume la comilla de cierre
        agregarToken(TokenType.STRING_CONST);
    }

    // Char: 'c'
    private void escanearChar() {
        if (finDelFuente() || vistazo() == '\n') {
            registrarError(linea, colInicio, "Constante char vacia o no cerrada");
            return;
        }
        avanzar(); // consume el caracter

        if (fuente.charAt(actual - 1) == '\\') {
            if (finDelFuente()) {
                registrarError(linea, colInicio, "Secuencia de escape incompleta");
                return;
            }
            avanzar();
        }

        if (vistazo() != '\'') {
            registrarError(linea, colInicio,
                    "Constante char debe tener exactamente un caracter");
            while (!finDelFuente() && vistazo() != '\'' && vistazo() != '\n') avanzar();
            if (!finDelFuente() && vistazo() == '\'') avanzar();
            return;
        }
        avanzar(); // consume la comilla de cierre
        agregarToken(TokenType.CHAR_CONST);
    }

    // Identificadores y palabras reservadas
    private void escanearIdentificador() {
        while (esLetraODigito(vistazo())) avanzar();
        String texto = fuente.substring(inicio, actual);
        TokenType tipo = PALABRAS_RESERVADAS.getOrDefault(texto, TokenType.IDENTIFIER);
        agregarToken(tipo);
    }

    // Comentario de bloque /* ... */
    private void comentarioBloque() {
        int lineaInicio = linea;
        int colIni = colInicio;
        while (!finDelFuente()) {
            if (vistazo() == '\n') { linea++; columna = 1; }
            if (vistazo() == '*' && vistazoDos() == '/') {
                avanzar();
                avanzar();
                return;
            }
            avanzar();
        }
        registrarError(lineaInicio, colIni, "Comentario de bloque no cerrado");
    }

    // Utilidades
    private char avanzar() {
        char c = fuente.charAt(actual++);
        columna++;
        return c;
    }

    private boolean coincide(char esperado) {
        if (finDelFuente() || fuente.charAt(actual) != esperado) return false;
        actual++;
        columna++;
        return true;
    }

    private char vistazo() {
        return finDelFuente() ? '\0' : fuente.charAt(actual);
    }

    private char vistazoDos() {
        return (actual + 1 >= fuente.length()) ? '\0' : fuente.charAt(actual + 1);
    }

    private boolean finDelFuente()       { return actual >= fuente.length(); }
    private boolean esDigito(char c)       { return c >= '0' && c <= '9'; }
    private boolean esLetra(char c)        { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'; }
    private boolean esLetraODigito(char c) { return esLetra(c) || esDigito(c); }

    private void agregarToken(TokenType tipo) {
        String lexema = fuente.substring(inicio, actual);
        tokens.add(new Token(tipo, lexema, linea, colInicio));
    }

    private void registrarError(int lin, int col, String msg) {
        errores.add(new ErrorCompilador(ErrorCompilador.Fase.LEXICA, lin, col, msg));
    }
}
