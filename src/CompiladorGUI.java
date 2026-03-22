import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CompiladorGUI extends JFrame {
    // Paleta de colores (tema oscuro)
    public static final Color BG_PRINCIPAL   = new Color(30,  30,  46);
    public static final Color BG_PANEL       = new Color(42,  42,  62);
    public static final Color BG_EDITOR      = new Color(24,  24,  37);
    public static final Color TEXT_PRINCIPAL = new Color(205, 214, 244);
    public static final Color TEXT_SECUNDARIO= new Color(166, 173, 200);
    public static final Color ACENTO         = new Color(137, 180, 250);
    public static final Color COLOR_EXITO    = new Color(166, 227, 161);
    public static final Color COLOR_ERROR    = new Color(243, 139, 168);
    public static final Color COLOR_WARN     = new Color(250, 179, 135);
    public static final Color COLOR_BORDE    = new Color(69,  71,  90);

    // Colores por tipo de token
    public static final Color TOK_RESERVADA  = new Color(203, 166, 247); // morado
    public static final Color TOK_IDENTIFIER = new Color(166, 227, 161); // verde
    public static final Color TOK_NUMBER     = new Color(250, 179, 135); // naranja
    public static final Color TOK_STRING     = new Color(166, 227, 161); // verde claro
    public static final Color TOK_CHAR       = new Color(245, 194, 231); // rosa
    public static final Color TOK_OPERADOR   = new Color(137, 180, 250); // azul
    public static final Color TOK_DELIMITADOR= new Color(243, 139, 168); // rojo suave

    // Ejemplos
    public static final String EJEMPLO_BASICO =
        "int x = 10;\n" +
        "int y = 3;\n" +
        "string saludo = \"Hola mundo\";\n" +
        "char letra = 'A';\n" +
        "print(x);\n" +
        "print(saludo);\n" +
        "print(letra);\n";

    public static final String EJEMPLO_CONTROL =
        "int x = 10;\n" +
        "int y = 5;\n" +
        "\n" +
        "if (x > y) {\n" +
        "    int z = x - y;\n" +
        "    print(z);\n" +
        "} else {\n" +
        "    print(y);\n" +
        "}\n" +
        "\n" +
        "int i = 1;\n" +
        "while (i <= 3) {\n" +
        "    print(i);\n" +
        "    i = i + 1;\n" +
        "}\n" +
        "\n" +
        "int n = 5;\n" +
        "do {\n" +
        "    print(n);\n" +
        "    n = n - 1;\n" +
        "} while (n > 0);\n";

    public static final String EJEMPLO_ERRORES =
        "int x = 10;\n" +
        "string texto = \"sin cerrar;\n" +
        "int x = 20;\n" +
        "if x > 5 {\n" +
        "    print(x);\n" +
        "}\n" +
        "int num = \"hola\";\n" +
        "print(noExiste);\n" +
        "int z;\n" +
        "print(z);\n";

    private final EditorPanel editorPanel;
    private final ResultadosPanel resultadosPanel;
    private final BarraEstado barraEstado;

    public CompiladorGUI() {
        setTitle("Compilador - Analizador de Codigo");
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BG_PRINCIPAL);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PRINCIPAL);
        setContentPane(root);

        editorPanel = new EditorPanel(this);
        resultadosPanel = new ResultadosPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPanel, resultadosPanel);
        splitPane.setDividerLocation(0.4);
        splitPane.setResizeWeight(0.4);
        splitPane.setBorder(null);
        splitPane.setBackground(BG_PRINCIPAL);
        root.add(splitPane, BorderLayout.CENTER);

        barraEstado = new BarraEstado();
        root.add(barraEstado, BorderLayout.SOUTH);

        // Cargar ejemplo inicial y compilar
        cargarEjemplo("Basico");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompiladorGUI().setVisible(true));
    }

    public void ejecutarCompilacion() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        barraEstado.setEstado("Compilando...", ACENTO);

        new SwingWorker<ResultadoCompilacion, Void>() {
            @Override
            protected ResultadoCompilacion doInBackground() {
                return CompiladorService.ejecutar(editorPanel.getCodigo());
            }

            @Override
            protected void done() {
                try {
                    ResultadoCompilacion r = get();
                    resultadosPanel.mostrarResultado(r);
                    barraEstado.actualizar(r);
                    seleccionarPestanaSegunResultado(r);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void seleccionarPestanaSegunResultado(ResultadoCompilacion r) {
        if (r.exitoso) {
            resultadosPanel.setSelectedIndex(3); // TAC
        } else {
            // Buscar primera fase con errores
            for (ErrorCompilador e : r.errores) {
                if (e.getFase() == ErrorCompilador.Fase.LEXICA) {
                    resultadosPanel.setSelectedIndex(0); // Tokens (donde se ven errores léxicos a veces)
                    return;
                }
                if (e.getFase() == ErrorCompilador.Fase.SINTACTICA) {
                    resultadosPanel.setSelectedIndex(1); // Sintactico
                    return;
                }
                if (e.getFase() == ErrorCompilador.Fase.SEMANTICA) {
                    resultadosPanel.setSelectedIndex(2); // Simbolos/Semantico
                    return;
                }
            }
        }
    }

    public void cargarEjemplo(String nombre) {
        switch (nombre) {
            case "Basico":
                editorPanel.setCodigo(EJEMPLO_BASICO);
                break;
            case "Control de flujo":
                editorPanel.setCodigo(EJEMPLO_CONTROL);
                break;
            case "Con errores":
                editorPanel.setCodigo(EJEMPLO_ERRORES);
                break;
            default:
                return;
        }
        ejecutarCompilacion();
    }

    public void limpiar() {
        editorPanel.limpiar();
        resultadosPanel.limpiar();
        barraEstado.limpiar();
    }

    private class BarraEstado extends JPanel {
        private final JLabel statusLabel;
        private final JLabel lblTokens, lblSimbolos, lblTac, lblErrores;
        private final JLabel lblHora;

        public BarraEstado() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(0, 80));
            setBackground(BG_PANEL);
            setBorder(new EmptyBorder(10, 20, 10, 20));

            // Lado izquierdo
            statusLabel = new JLabel("Listo", createCircleIcon(Color.GRAY), SwingConstants.LEFT);
            statusLabel.setForeground(TEXT_SECUNDARIO);
            statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            add(statusLabel, BorderLayout.WEST);

            // Centro - Contadores
            JPanel pContadores = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
            pContadores.setOpaque(false);
            lblTokens = crearContador(pContadores, "Tokens");
            lblSimbolos = crearContador(pContadores, "Simbolos");
            lblTac = crearContador(pContadores, "TAC");
            lblErrores = crearContador(pContadores, "Errores");
            add(pContadores, BorderLayout.CENTER);

            // Lado derecho
            JPanel pDer = new JPanel(new GridLayout(2, 1));
            pDer.setOpaque(false);
            JLabel lblArchivo = new JLabel("<sin archivo>", SwingConstants.RIGHT);
            lblArchivo.setForeground(TEXT_SECUNDARIO);
            lblHora = new JLabel("--:--:--", SwingConstants.RIGHT);
            lblHora.setForeground(TEXT_SECUNDARIO);
            pDer.add(lblArchivo);
            pDer.add(lblHora);
            add(pDer, BorderLayout.EAST);
        }

        private JLabel crearContador(JPanel parent, String desc) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            JLabel n = new JLabel("0", SwingConstants.CENTER);
            n.setFont(new Font("SansSerif", Font.BOLD, 18));
            n.setForeground(ACENTO);
            JLabel d = new JLabel(desc.toUpperCase(), SwingConstants.CENTER);
            d.setFont(new Font("SansSerif", Font.PLAIN, 11));
            d.setForeground(TEXT_SECUNDARIO);
            p.add(n, BorderLayout.CENTER);
            p.add(d, BorderLayout.SOUTH);

            parent.add(p);
            // Retornamos el label del número para actualizarlo
            return n;
        }

        public void setEstado(String texto, Color color) {
            statusLabel.setText(texto);
            statusLabel.setIcon(createCircleIcon(color));
            statusLabel.setForeground(color);
        }

        public void actualizar(ResultadoCompilacion r) {
            if (r.exitoso) {
                setEstado("Exito", COLOR_EXITO);
            } else {
                setEstado(r.errores.size() + " error(es)", COLOR_ERROR);
            }

            lblTokens.setText(String.valueOf(Math.max(0, r.tokens.size() - 1)));
            lblSimbolos.setText(String.valueOf(r.simbolos.size()));
            lblTac.setText(String.valueOf(r.instruccionesTAC.size()));
            lblErrores.setText(String.valueOf(r.errores.size()));

            lblHora.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }

        public void limpiar() {
            setEstado("Listo", Color.GRAY);
            lblTokens.setText("0");
            lblSimbolos.setText("0");
            lblTac.setText("0");
            lblErrores.setText("0");
            lblHora.setText("--:--:--");
        }

        private Icon createCircleIcon(Color color) {
            BufferedImage img = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 0, 11, 11);
            g2.dispose();
            return new ImageIcon(img);
        }
    }
}
