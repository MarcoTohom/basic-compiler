public enum TokenType {
    // Palabras reservadas
    IF, ELSE, WHILE, DO, INT, STRING, CHAR, PRINT,

    // Identificadores y literales
    IDENTIFIER,
    NUMBER,
    STRING_CONST,
    CHAR_CONST,

    // Operadores aritmeticos
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,

    // Operadores relacionales
    EQ,
    NEQ,
    LT,
    GT,
    LEQ,
    GEQ,

    // Asignacion
    ASSIGN,

    // Delimitadores
    SEMICOLON,
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,

    // Especiales
    EOF,
    UNKNOWN
}
