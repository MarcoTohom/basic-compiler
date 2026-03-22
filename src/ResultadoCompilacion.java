import java.util.List;

public class ResultadoCompilacion {
    public List<Token> tokens;
    public List<TablaSimbolos.Simbolo> simbolos;
    public List<GeneradorCodigo.Instruccion> instruccionesTAC;
    public List<ErrorCompilador> errores;
    public boolean exitoso;

    public ResultadoCompilacion(List<Token> tokens, List<TablaSimbolos.Simbolo> simbolos,
                               List<GeneradorCodigo.Instruccion> instruccionesTAC,
                               List<ErrorCompilador> errores, boolean exitoso) {
        this.tokens = tokens;
        this.simbolos = simbolos;
        this.instruccionesTAC = instruccionesTAC;
        this.errores = errores;
        this.exitoso = exitoso;
    }

    public List<Token> getTokens() { return tokens; }
    public List<TablaSimbolos.Simbolo> getSimbolos() { return simbolos; }
    public List<GeneradorCodigo.Instruccion> getInstruccionesTAC() { return instruccionesTAC; }
    public List<ErrorCompilador> getErrores() { return errores; }
    public boolean isExitoso() { return exitoso; }
}
