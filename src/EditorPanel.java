import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class EditorPanel extends JPanel {
    private final JTextArea editorTexto;
    private final LineNumberPanel lineNumberPanel;
    private final JComboBox<String> comboEjemplos;
    private final CompiladorGUI gui;

    public EditorPanel(CompiladorGUI gui) {
        this.gui = gui;
        setLayout(new BorderLayout());
        setBackground(CompiladorGUI.BG_PANEL);

        // Barra de herramientas superior
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBackground(CompiladorGUI.BG_PANEL);
        toolbar.setBorder(new EmptyBorder(5, 5, 5, 5));

        JButton btnCompilar = new JButton("Compilar [F5]");
        btnCompilar.setBackground(CompiladorGUI.ACENTO);
        btnCompilar.setForeground(Color.WHITE);
        btnCompilar.setFocusPainted(false);
        btnCompilar.addActionListener(e -> gui.ejecutarCompilacion());

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> gui.limpiar());

        comboEjemplos = new JComboBox<>(new String[]{"-- Seleccionar --", "Basico", "Control de flujo", "Con errores"});
        comboEjemplos.addActionListener(e -> gui.cargarEjemplo((String) comboEjemplos.getSelectedItem()));

        toolbar.add(btnCompilar);
        toolbar.add(Box.createRigidArea(new Dimension(10, 0)));
        toolbar.add(btnLimpiar);
        toolbar.addSeparator();
        toolbar.add(new JLabel(" Ejemplos: "));
        toolbar.add(comboEjemplos);

        add(toolbar, BorderLayout.NORTH);

        // Area de edicion con numeros de linea
        editorTexto = new JTextArea();
        editorTexto.setFont(new Font("Consolas", Font.PLAIN, 14));
        editorTexto.setBackground(CompiladorGUI.BG_EDITOR);
        editorTexto.setForeground(CompiladorGUI.TEXT_PRINCIPAL);
        editorTexto.setCaretColor(CompiladorGUI.ACENTO);
        editorTexto.setTabSize(4);
        editorTexto.setLineWrap(false);

        JScrollPane scrollPane = new JScrollPane(editorTexto);
        scrollPane.setBorder(null);

        lineNumberPanel = new LineNumberPanel(editorTexto);
        scrollPane.setRowHeaderView(lineNumberPanel);

        add(scrollPane, BorderLayout.CENTER);

        // Atajo F5
        editorTexto.getInputMap().put(KeyStroke.getKeyStroke("F5"), "compilar");
        editorTexto.getActionMap().put("compilar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.ejecutarCompilacion();
            }
        });

        // Listener para actualizar números de línea
        editorTexto.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { lineNumberPanel.repaint(); }
            @Override public void removeUpdate(DocumentEvent e) { lineNumberPanel.repaint(); }
            @Override public void changedUpdate(DocumentEvent e) { lineNumberPanel.repaint(); }
        });
    }

    public String getCodigo() {
        return editorTexto.getText();
    }

    public void setCodigo(String codigo) {
        editorTexto.setText(codigo);
        lineNumberPanel.repaint();
    }

    public void limpiar() {
        editorTexto.setText("");
        comboEjemplos.setSelectedIndex(0);
        lineNumberPanel.repaint();
    }

    private static class LineNumberPanel extends JComponent {
        private final JTextArea textArea;

        public LineNumberPanel(JTextArea textArea) {
            this.textArea = textArea;
            setBackground(CompiladorGUI.BG_PANEL);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(45, textArea.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(CompiladorGUI.BG_PANEL);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setFont(textArea.getFont());
            g.setColor(CompiladorGUI.TEXT_SECUNDARIO);

            FontMetrics metrics = g.getFontMetrics();
            int lineHeight = metrics.getHeight();
            int lineCount = textArea.getLineCount();

            for (int i = 0; i < lineCount; i++) {
                String lineNum = String.valueOf(i + 1);
                int y = (i * lineHeight) + metrics.getAscent() + 2; // Ajuste manual
                int x = getWidth() - metrics.stringWidth(lineNum) - 8;
                g.drawString(lineNum, x, y);
            }
        }
    }
}
