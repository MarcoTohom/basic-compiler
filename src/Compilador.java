import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * COMPILADOR - Orquestador de fases
 * ==================================
 * Fases:
 *   1. Analisis Lexico   (Lexer)
 *   2. Analisis Sintactico (Parser - descenso recursivo)
 *   3. Analisis Semantico (integrado en el Parser + TablaSimbolos)
 *   4. Generacion de Codigo Intermedio (TAC - GeneradorCodigo)
 *
 * Uso:
 *   java Compilador <archivo_fuente>
 *   java Compilador          (usa el programa de prueba integrado)
 */
public class Compilador {

    private static final String PROGRAMA_DEMO =
        "// Programa de demostracion\n" +
        "int x = 10;\n" +
        "int y = 5;\n" +
        "int z;\n" +
        "\n" +
        "if (x > y) {\n" +
        "    z = x - y;\n" +
        "    print(z);\n" +
        "} else {\n" +
        "    z = y - x;\n" +
        "    print(z);\n" +
        "}\n" +
        "\n" +
        "int contador = 0;\n" +
        "while (contador < 3) {\n" +
        "    print(contador);\n" +
        "    contador = contador + 1;\n" +
        "}\n" +
        "\n" +
        "int i = 1;\n" +
        "do {\n" +
        "    print(i);\n" +
        "    i = i + 1;\n" +
        "} while (i < 4);\n" +
        "\n" +
        "string nombre = \"mundo\";\n" +
        "char inicial = 'M';\n" +
        "print(nombre);\n";

    public static void main(String[] args) {
        String codigoFuente;
        String nombreArchivo;

        if (args.length > 0) {
            nombreArchivo = args[0];
            try {
                codigoFuente = new String(Files.readAllBytes(Paths.get(nombreArchivo)));
            } catch (IOException e) {
                System.err.println("Error: No se pudo leer el archivo '" + nombreArchivo + "'");
                System.exit(1);
                return;
            }
        } else {
            lanzarGUI();
            return;
        }

        compilar(codigoFuente, nombreArchivo);
    }

    public static void lanzarGUI() {
        SwingUtilities.invokeLater(() -> new CompiladorGUI().setVisible(true));
    }

    public static void compilar(String codigoFuente, String nombreArchivo) {
        linea('=', 62);
        System.out.println("  COMPILADOR - Procesando: " + nombreArchivo);
        linea('=', 62);
        System.out.println();

        List<ErrorCompilador> errores = new ArrayList<>();

        // ------------------------------------------------------------------
        // FASE 1 - ANALISIS LEXICO
        // ------------------------------------------------------------------
        seccion("FASE 1 - ANALISIS LEXICO");
        Lexer lexer = new Lexer(codigoFuente, errores);
        List<Token> tokens = lexer.tokenizar();

        System.out.println("  Tokens generados: " + (tokens.size() - 1));
        System.out.println();
        for (Token t : tokens) {
            if (t.tipo != TokenType.EOF) {
                System.out.println("  " + t);
            }
        }

        int errLex = contarErrores(errores, ErrorCompilador.Fase.LEXICA);
        System.out.println();
        if (errLex > 0) {
            System.out.println("  AVISO: " + errLex + " error(es) lexico(s).");
        } else {
            System.out.println("  OK: Sin errores lexicos.");
        }

        // ------------------------------------------------------------------
        // FASES 2, 3 y 4 - SINTACTICO + SEMANTICO + GENERACION DE CODIGO
        // ------------------------------------------------------------------
        seccion("FASES 2, 3 y 4 - SINTACTICO + SEMANTICO + CODIGO");

        TablaSimbolos tabla = new TablaSimbolos();
        GeneradorCodigo gen = new GeneradorCodigo();
        Parser parser = new Parser(tokens, errores, tabla, gen);
        parser.parsearPrograma();

        System.out.println("  Errores sintacticos : " + contarErrores(errores, ErrorCompilador.Fase.SINTACTICA));
        System.out.println("  Errores semanticos  : " + contarErrores(errores, ErrorCompilador.Fase.SEMANTICA));

        tabla.imprimir();

        if (errores.isEmpty()) {
            gen.imprimir();
        } else {
            System.out.println();
            System.out.println("  (Codigo intermedio omitido por errores)");
        }

        // ------------------------------------------------------------------
        // REPORTE FINAL
        // ------------------------------------------------------------------
        seccion("REPORTE FINAL DE ERRORES");

        if (errores.isEmpty()) {
            System.out.println("  EXITO: Compilacion exitosa. Sin errores.");
        } else {
            System.out.println("  Total de errores: " + errores.size());
            System.out.println();
            for (ErrorCompilador e : errores) {
                System.out.println("  " + e);
            }
            System.out.println();
            System.out.println("  FALLO: Compilacion terminada con errores.");
        }
        System.out.println();
    }

    private static void linea(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        System.out.println(sb.toString());
    }

    private static void seccion(String titulo) {
        System.out.println();
        System.out.println("--- " + titulo + " ---");
        System.out.println();
    }

    private static int contarErrores(List<ErrorCompilador> errores, ErrorCompilador.Fase fase) {
        int n = 0;
        for (ErrorCompilador e : errores) {
            if (e.getFase() == fase) n++;
        }
        return n;
    }
}
