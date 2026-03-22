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

    public static void main(String[] args) {
        lanzarGUI();
    }

    public static void lanzarGUI() {
        SwingUtilities.invokeLater(() -> new CompiladorGUI().setVisible(true));
    }

        int n = 0;
        for (ErrorCompilador e : errores) {
            if (e.getFase() == fase) n++;
        }
        return n;
    }

