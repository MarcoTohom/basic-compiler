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
    private final JTable tablaSimbolos;
    private final DefaultTableModel modeloSimbolos;
    private final JLabel simbolosCountLabel;
    private final ErrorPanel erroresSemanticosPanel;

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
    }

    public void mostrarResultado(ResultadoCompilacion r) {
        // Tokens
        tokensCountLabel.setText("Total: " + (r.tokens.size() - 1) + " tokens generados");
        TokenRenderer.renderTokens(tokensPane, r.tokens);

       
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
        modeloSimbolos.setRowCount(0);
        simbolosCountLabel.setText("Total: 0 simbolos declarados");
        erroresSemanticosPanel.setErrores(null);
    }
}
