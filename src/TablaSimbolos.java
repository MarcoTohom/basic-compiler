import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * TABLA DE SIMBOLOS con soporte de ambitos anidados.
 *
 * Cada bloque { } abre un nuevo ambito. La busqueda de un simbolo
 * recorre los ambitos de mas interno a mas externo (cadena de ambitos).
 */
public class TablaSimbolos {

    // Entrada de la tabla
    public static class Simbolo {
        public final String  nombre;
        public final String  tipo;        // "int" | "string" | "char"
        public final int     linea;       // linea de declaracion
        public       String  valor;       // valor actual
        public       boolean inicializado;

        public Simbolo(String nombre, String tipo, int linea) {
            this.nombre       = nombre;
            this.tipo         = tipo;
            this.linea        = linea;
            this.valor        = null;
            this.inicializado = false;
        }

        @Override
        public String toString() {
            return String.format("Simbolo{ nombre=%-12s tipo=%-8s linea=%d valor=%s }",
                    nombre, tipo, linea, valor == null ? "<sin valor>" : valor);
        }
    }

    // Pila de ambitos
    private final Stack<Map<String, Simbolo>> pilaAmbitos = new Stack<>();

    public TablaSimbolos() {
        abrirAmbito(); // ambito global
    }

    // Gestion de ambitos
    public void abrirAmbito() {
        pilaAmbitos.push(new HashMap<>());
    }

    public void cerrarAmbito() {
        if (pilaAmbitos.size() > 1) {
            pilaAmbitos.pop();
        }
    }

    public int getNivel() {
        return pilaAmbitos.size();
    }

    /**
     * Declara un nuevo simbolo en el ambito actual.
     * Retorna true si se declaro; false si ya existia en el mismo ambito.
     */
    public boolean declarar(String nombre, String tipo, int linea) {
        Map<String, Simbolo> ambitoActual = pilaAmbitos.peek();
        if (ambitoActual.containsKey(nombre)) return false;
        ambitoActual.put(nombre, new Simbolo(nombre, tipo, linea));
        return true;
    }

    /**
     * Busca un simbolo empezando en el ambito mas interno.
     * Retorna el Simbolo, o null si no existe.
     */
    public Simbolo buscar(String nombre) {
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--) {
            Simbolo s = pilaAmbitos.get(i).get(nombre);
            if (s != null) return s;
        }
        return null;
    }

    public boolean setValor(String nombre, String valor) {
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--) {
            Simbolo s = pilaAmbitos.get(i).get(nombre);
            if (s != null) {
                s.valor        = valor;
                s.inicializado = true;
                return true;
            }
        }
        return false;
    }

    public List<Simbolo> obtenerTodos() {
        List<Simbolo> todos = new ArrayList<>();
        for (Map<String, Simbolo> ambito : pilaAmbitos) {
            todos.addAll(ambito.values());
        }
        return todos;
    }

    public void imprimir() {
        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("             TABLA DE SIMBOLOS");
        System.out.println("--------------------------------------------------");
        for (int i = 0; i < pilaAmbitos.size(); i++) {
            System.out.println("  Ambito nivel " + i + ":");
            if (pilaAmbitos.get(i).isEmpty()) {
                System.out.println("    (vacio)");
            }
            for (Simbolo s : pilaAmbitos.get(i).values()) {
                System.out.printf("    %-12s  tipo: %-8s  linea: %d  valor: %s%n",
                        s.nombre, s.tipo, s.linea,
                        s.valor == null ? "<sin valor>" : s.valor);
            }
        }
        System.out.println("--------------------------------------------------");
    }
}
