/**
 * Representa un token producido por el analisis lexico.
 * Cada token tiene un tipo, el texto original (lexema),
 * y su posicion en el codigo fuente.
 */
public class Token {
    public final TokenType tipo;
    public final String    lexema;
    public final int       linea;
    public final int       columna;

    public Token(TokenType tipo, String lexema, int linea, int columna) {
        this.tipo    = tipo;
        this.lexema  = lexema;
        this.linea   = linea;
        this.columna = columna;
    }

    @Override
    public String toString() {
        return String.format("Token[%-14s | %-20s | linea %3d, col %3d]",
                tipo, "\"" + lexema + "\"", linea, columna);
    }
}
