import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

public class TokenRenderer {

    public static void renderTokens(JTextPane textPane, List<Token> tokens) {
        textPane.setText("");
        StyledDocument doc = textPane.getStyledDocument();

        for (Token token : tokens) {
            if (token.tipo == TokenType.EOF) continue;

            Style style = textPane.addStyle("token", null);
            Color bgColor = getColorForTokenType(token.tipo);
            Color fgColor = getContrastColor(bgColor, token.tipo);

            StyleConstants.setBackground(style, bgColor);
            StyleConstants.setForeground(style, fgColor);
            StyleConstants.setBold(style, esPalabraReservada(token.tipo));
            StyleConstants.setFontFamily(style, "Consolas");
            StyleConstants.setFontSize(style, 13);

            try {
                doc.insertString(doc.getLength(), " " + token.lexema + " ", style);
                doc.insertString(doc.getLength(), "  ", null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private static Color getColorForTokenType(TokenType tipo) {
        switch (tipo) {
            case INT: case STRING: case CHAR:
            case IF: case ELSE: case WHILE: case DO: case PRINT:
                return CompiladorGUI.TOK_RESERVADA;
            case IDENTIFIER:
                return CompiladorGUI.TOK_IDENTIFIER;
            case NUMBER:
                return CompiladorGUI.TOK_NUMBER;
            case STRING_CONST:
                return CompiladorGUI.TOK_STRING;
            case CHAR_CONST:
                return CompiladorGUI.TOK_CHAR;
            case PLUS: case MINUS: case MULTIPLY: case DIVIDE:
            case ASSIGN: case GT: case LT: case GEQ:
            case LEQ: case EQ: case NEQ:
                return CompiladorGUI.TOK_OPERADOR;
            default:
                return CompiladorGUI.TOK_DELIMITADOR;
        }
    }

    private static Color getContrastColor(Color bg, TokenType tipo) {
        if (esPalabraReservada(tipo)) {
            return new Color(49, 20, 83);
        }
        if (tipo == TokenType.IDENTIFIER) {
            return new Color(22, 58, 37);
        }
        return bg.darker().darker();
    }

    private static boolean esPalabraReservada(TokenType tipo) {
        return tipo == TokenType.INT || tipo == TokenType.STRING || tipo == TokenType.CHAR ||
               tipo == TokenType.IF || tipo == TokenType.ELSE || tipo == TokenType.WHILE ||
               tipo == TokenType.DO || tipo == TokenType.PRINT;
    }
}
