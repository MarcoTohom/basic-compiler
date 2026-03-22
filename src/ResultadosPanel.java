import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

public class ResultadosPanel extends JTabbedPane {
    private final JTextPane tokensPane;
    private final JLabel tokensCountLabel;
    private final ErrorPanel erroresSintacticosPanel;
    private final JLabel sintacticoStatusLabel;
    private final JTable tablaSimbolos;
    private final DefaultTableModel modeloSimbolos;
    private final JLabel simbolosCountLabel;
    private final ErrorPanel erroresSemanticosPanel;
    private final JTextPane tacPane;
    private final JLabel tacErrorLabel;

    public ResultadosPanel() {
        setTabPlacement(JTabbedPane.TOP);
        setBackground(CompiladorGUI.BG_PANEL);
        setForeground(CompiladorGUI.TEXT_PRINCIPAL);

        // Pestana 1: Tokens
        JPanel pTokens = new JPanel(new BorderLayout());
        pTokens.setBackground(CompiladorGUI.BG_EDITOR);
        tokensCountLabel = new JLabel("Total: 0 tokens generados");
        tokensCountLabel.setForeground(CompiladorGUI.TEXT_SECUNDARIO);
        tokensCountLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        pTokens.add(tokensCountLabel, BorderLayout.NORTH);

        tokensPane = new JTextPane();
        tokensPane.setEditable(false);
        tokensPane.setBackground(CompiladorGUI.BG_EDITOR);
        JScrollPane scrollTokens = new JScrollPane(tokensPane);
        scrollTokens.setBorder(null);
        pTokens.add(scrollTokens, BorderLayout.CENTER);
        addTab("Tokens (Fase 1)", pTokens);

        // Pestana 2: Sintactico
        JPanel pSintactico = new JPanel(new BorderLayout());
        pSintactico.setBackground(CompiladorGUI.BG_EDITOR);
        sintacticoStatusLabel = new JLabel("Esperando...");
        sintacticoStatusLabel.setForeground(CompiladorGUI.TEXT_SECUNDARIO);
        sintacticoStatusLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        pSintactico.add(sintacticoStatusLabel, BorderLayout.NORTH);
        erroresSintacticosPanel = new ErrorPanel();
        pSintactico.add(erroresSintacticosPanel, BorderLayout.CENTER);
        addTab("Sintactico (Fase 2)", pSintactico);

        // Pestana 3: Simbolos
        JPanel pSimbolos = new JPanel(new BorderLayout());
        pSimbolos.setBackground(CompiladorGUI.BG_EDITOR);
        simbolosCountLabel = new JLabel("Total: 0 simbolos declarados");
        simbolosCountLabel.setForeground(CompiladorGUI.TEXT_SECUNDARIO);
        simbolosCountLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        pSimbolos.add(simbolosCountLabel, BorderLayout.NORTH);

        String[] cols = {"Nombre", "Tipo", "Linea decl.", "Estado", "Valor"};
        modeloSimbolos = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaSimbolos = new JTable(modeloSimbolos);
        configurarTabla(tablaSimbolos);
        JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
        scrollSimbolos.getViewport().setBackground(CompiladorGUI.BG_PANEL);
        pSimbolos.add(scrollSimbolos, BorderLayout.CENTER);

        erroresSemanticosPanel = new ErrorPanel();
        erroresSemanticosPanel.setPreferredSize(new Dimension(0, 150));
        pSimbolos.add(erroresSemanticosPanel, BorderLayout.SOUTH);
        addTab("Simbolos (Fase 3)", pSimbolos);

        // Pestana 4: Codigo TAC
        JPanel pTac = new JPanel(new BorderLayout());
        pTac.setBackground(CompiladorGUI.BG_EDITOR);
        tacErrorLabel = new JLabel("Codigo intermedio omitido — corrija los errores primero");
        tacErrorLabel.setForeground(CompiladorGUI.COLOR_WARN);
        tacErrorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tacErrorLabel.setVisible(false);
        pTac.add(tacErrorLabel, BorderLayout.NORTH);

        tacPane = new JTextPane();
        tacPane.setEditable(false);
        tacPane.setBackground(CompiladorGUI.BG_EDITOR);
        tacPane.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane scrollTac = new JScrollPane(tacPane);
        scrollTac.setBorder(null);
        pTac.add(scrollTac, BorderLayout.CENTER);
        addTab("Codigo TAC (Fase 4)", pTac);
    }

    public void mostrarResultado(ResultadoCompilacion r) {
        // Tokens
        tokensCountLabel.setText("Total: " + (r.tokens.size() - 1) + " tokens generados");
        TokenRenderer.renderTokens(tokensPane, r.tokens);

        // Sintactico
        List<ErrorCompilador> errSint = filtrarErrores(r.errores, ErrorCompilador.Fase.SINTACTICA);
        if (errSint.isEmpty()) {
            sintacticoStatusLabel.setText("Estructura sintactica valida");
            sintacticoStatusLabel.setForeground(CompiladorGUI.COLOR_EXITO);
        } else {
            sintacticoStatusLabel.setText(errSint.size() + " errores sintacticos");
            sintacticoStatusLabel.setForeground(CompiladorGUI.COLOR_ERROR);
        }
        erroresSintacticosPanel.setErrores(errSint);

        // Simbolos
        simbolosCountLabel.setText("Total: " + r.simbolos.size() + " simbolos declarados");
        modeloSimbolos.setRowCount(0);
        for (TablaSimbolos.Simbolo s : r.simbolos) {
            modeloSimbolos.addRow(new Object[]{
                s.nombre, s.tipo, s.linea,
                s.inicializado ? "inicializado" : "sin valor",
                s.valor == null ? "-" : s.valor
            });
        }
        erroresSemanticosPanel.setErrores(filtrarErrores(r.errores, ErrorCompilador.Fase.SEMANTICA));

        // TAC
        tacPane.setText("");
        if (r.exitoso) {
            tacErrorLabel.setVisible(false);
            renderizarTAC(r.instruccionesTAC);
        } else {
            tacErrorLabel.setVisible(true);
        }
    }

    private void renderizarTAC(List<GeneradorCodigo.Instruccion> instrucciones) {
        StyledDocument doc = tacPane.getStyledDocument();
        int n = 0;
        for (GeneradorCodigo.Instruccion ins : instrucciones) {
            String codigo = ins.codigo;
            Style style = tacPane.addStyle("tac", null);
            StyleConstants.setForeground(style, CompiladorGUI.TEXT_PRINCIPAL);

            try {
                // Numero de linea
                Style sNum = tacPane.addStyle("num", null);
                StyleConstants.setForeground(sNum, CompiladorGUI.TEXT_SECUNDARIO);
                if (!codigo.endsWith(":")) {
                    doc.insertString(doc.getLength(), String.format("  %3d  ", n++), sNum);
                } else {
                    doc.insertString(doc.getLength(), "       ", sNum);
                }

                if (codigo.endsWith(":")) {
                    StyleConstants.setForeground(style, CompiladorGUI.COLOR_WARN);
                    StyleConstants.setBold(style, true);
                    doc.insertString(doc.getLength(), codigo + "\n", style);
                } else {
                    // Syntax highlighting simple (solo palabras clave)
                    String[] words = codigo.split(" ");
                    for (String word : words) {
                        Style sWord = tacPane.addStyle("word", style);
                        if (word.equals("IF") || word.equals("GOTO")) {
                            StyleConstants.setForeground(sWord, CompiladorGUI.ACENTO);
                            StyleConstants.setBold(sWord, true);
                        } else if (word.equals("PRINT")) {
                            StyleConstants.setForeground(sWord, CompiladorGUI.COLOR_EXITO);
                            StyleConstants.setBold(sWord, true);
                        } else if (word.equals("HALT")) {
                            StyleConstants.setForeground(sWord, CompiladorGUI.COLOR_ERROR);
                            StyleConstants.setBold(sWord, true);
                        } else if (word.matches("t\\d+")) {
                            StyleConstants.setForeground(sWord, CompiladorGUI.TOK_RESERVADA);
                        }
                        doc.insertString(doc.getLength(), word + " ", sWord);
                    }
                    doc.insertString(doc.getLength(), "\n", style);
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void configurarTabla(JTable tabla) {
        tabla.setBackground(CompiladorGUI.BG_PANEL);
        tabla.setForeground(CompiladorGUI.TEXT_PRINCIPAL);
        tabla.setRowHeight(28);
        tabla.setGridColor(CompiladorGUI.COLOR_BORDE);
        tabla.getTableHeader().setBackground(CompiladorGUI.BG_EDITOR);
        tabla.getTableHeader().setForeground(CompiladorGUI.ACENTO);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                c.setBackground(row % 2 == 0 ? CompiladorGUI.BG_PANEL : new Color(48, 48, 68));
                c.setForeground(CompiladorGUI.TEXT_PRINCIPAL);

                if (col == 1) { // Columna Tipo
                    String tipo = (String) value;
                    if ("int".equals(tipo)) {
                        c.setBackground(new Color(30, 50, 90));
                        c.setForeground(CompiladorGUI.ACENTO);
                    } else if ("string".equals(tipo)) {
                        c.setBackground(new Color(30, 70, 50));
                        c.setForeground(CompiladorGUI.COLOR_EXITO);
                    } else if ("char".equals(tipo)) {
                        c.setBackground(new Color(80, 30, 60));
                        c.setForeground(CompiladorGUI.TOK_CHAR);
                    }
                }

                if (col == 3) { // Columna Estado
                    String estado = (String) value;
                    if ("inicializado".equals(estado)) c.setForeground(CompiladorGUI.COLOR_EXITO);
                    else c.setForeground(CompiladorGUI.COLOR_WARN);
                }

                if (isSelected) c.setBackground(CompiladorGUI.ACENTO.darker());
                return c;
            }
        });
    }

    private List<ErrorCompilador> filtrarErrores(List<ErrorCompilador> errs, ErrorCompilador.Fase fase) {
        List<ErrorCompilador> filtrados = new java.util.ArrayList<>();
        for (ErrorCompilador e : errs) if (e.getFase() == fase) filtrados.add(e);
        return filtrados;
    }

    public void limpiar() {
        tokensPane.setText("");
        tokensCountLabel.setText("Total: 0 tokens generados");
        sintacticoStatusLabel.setText("Esperando...");
        sintacticoStatusLabel.setForeground(CompiladorGUI.TEXT_SECUNDARIO);
        erroresSintacticosPanel.setErrores(null);
        modeloSimbolos.setRowCount(0);
        simbolosCountLabel.setText("Total: 0 simbolos declarados");
        erroresSemanticosPanel.setErrores(null);
        tacPane.setText("");
        tacErrorLabel.setVisible(false);
    }
}
