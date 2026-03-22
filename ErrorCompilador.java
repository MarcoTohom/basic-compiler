/**
 * Almacena un error de compilacion con informacion completa
 * sobre la fase, posicion y mensaje descriptivo.
 */
public class ErrorCompilador {

    public enum Fase { LEXICA, SINTACTICA, SEMANTICA }

    private final Fase   fase;
    private final int    linea;
    private final int    columna;
    private final String mensaje;

    public ErrorCompilador(Fase fase, int linea, int columna, String mensaje) {
        this.fase    = fase;
        this.linea   = linea;
        this.columna = columna;
        this.mensaje = mensaje;
    }

    public Fase   getFase()    { return fase;    }
    public int    getLinea()   { return linea;   }
    public int    getColumna() { return columna; }
    public String getMensaje() { return mensaje; }

    @Override
    public String toString() {
        return String.format("[ERROR %-10s] Linea %3d, Col %3d -> %s",
                fase, linea, columna, mensaje);
    }
}
