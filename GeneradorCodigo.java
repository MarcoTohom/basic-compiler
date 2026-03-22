import java.util.ArrayList;
import java.util.List;

/**
 * FASE 4 - GENERACION DE CODIGO INTERMEDIO (Codigo de Tres Direcciones / TAC)
 *
 * Genera instrucciones TAC de la forma:
 *   t1 = a + b
 *   IF t1 > 0 GOTO L1
 *   GOTO L2
 *   LABEL L1
 *   PRINT x
 */
public class GeneradorCodigo {

    public static class Instruccion {
        public final String codigo;

        Instruccion(String codigo) {
            this.codigo = codigo;
        }

        @Override
        public String toString() {
            return codigo;
        }
    }

    private final List<Instruccion> instrucciones = new ArrayList<>();
    private int contadorTemp  = 0;
    private int contadorLabel = 0;

    public String nuevoTemp()  { return "t" + (++contadorTemp);  }
    public String nuevaLabel() { return "L" + (++contadorLabel); }

    /** Asignacion: dest = src */
    public void emitirAsig(String dest, String src) {
        emit(dest + " = " + src);
    }

    /** Operacion binaria: dest = op1 operador op2 */
    public void emitirBinaria(String dest, String op1, String operador, String op2) {
        emit(dest + " = " + op1 + " " + operador + " " + op2);
    }

    /** Salto condicional: IF cond GOTO label */
    public void emitirSaltoCond(String cond, String label) {
        emit("IF " + cond + " GOTO " + label);
    }

    /** Salto incondicional: GOTO label */
    public void emitirSalto(String label) {
        emit("GOTO " + label);
    }

    /** Definicion de etiqueta */
    public void emitirLabel(String label) {
        emit(label + ":");
    }

    /** Instruccion PRINT */
    public void emitirPrint(String valor) {
        emit("PRINT " + valor);
    }

    /** Instruccion HALT */
    public void emitirHalt() {
        emit("HALT");
    }

    public List<Instruccion> getInstrucciones() {
        return instrucciones;
    }

    public void imprimir() {
        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("       CODIGO INTERMEDIO (TAC)");
        System.out.println("--------------------------------------------------");
        int n = 0;
        for (Instruccion ins : instrucciones) {
            if (ins.codigo.endsWith(":")) {
                // etiqueta: sin numeracion
                System.out.printf("       %-40s%n", ins.codigo);
            } else {
                System.out.printf("  %3d  %-40s%n", n++, ins.codigo);
            }
        }
        System.out.println("--------------------------------------------------");
    }

    private void emit(String codigo) {
        instrucciones.add(new Instruccion(codigo));
    }
}
