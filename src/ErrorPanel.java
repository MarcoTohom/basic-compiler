import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

public class ErrorPanel extends JPanel {
    private final JPanel container;

    public ErrorPanel() {
        setLayout(new BorderLayout());
        setBackground(CompiladorGUI.BG_EDITOR);

        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CompiladorGUI.BG_EDITOR);

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setErrores(List<ErrorCompilador> errores) {
        container.removeAll();
        if (errores == null || errores.isEmpty()) {
            container.revalidate();
            container.repaint();
            return;
        }

        for (ErrorCompilador error : errores) {
            container.add(crearFilaError(error));
            container.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        container.revalidate();
        container.repaint();
    }

    private JPanel crearFilaError(ErrorCompilador error) {
        JPanel panel = new JPanel(new BorderLayout());
        Color colorBorde;

        switch (error.getFase()) {
            case LEXICA:
                colorBorde = CompiladorGUI.COLOR_WARN;
                break;
            case SINTACTICA:
                colorBorde = new Color(235, 160, 80);
                break;
            case SEMANTICA:
            default:
                colorBorde = CompiladorGUI.COLOR_ERROR;
                break;
        }

        Color bgBase = new Color(colorBorde.getRed(), colorBorde.getGreen(), colorBorde.getBlue(), 30);
        panel.setBackground(bgBase);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(0, 4, 0, 0, colorBorde),
                new EmptyBorder(8, 12, 8, 12)
        ));

        String texto = String.format("[%s] Linea %d, Col %d  ->  %s",
                error.getFase(), error.getLinea(), error.getColumna(), error.getMensaje());

        JLabel label = new JLabel(texto);
        label.setForeground(CompiladorGUI.TEXT_PRINCIPAL);
        label.setFont(new Font("Monospaced", Font.PLAIN, 13));

        panel.add(label, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

        return panel;
    }
}
