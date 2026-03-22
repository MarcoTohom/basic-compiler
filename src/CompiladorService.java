import java.util.ArrayList;
import java.util.List;

public class CompiladorService {
    public static ResultadoCompilacion ejecutar(String codigoFuente) {
        List<ErrorCompilador> errores = new ArrayList<>();

        Lexer lexer = new Lexer(codigoFuente, errores);
        List<Token> tokens = lexer.tokenizar();

        TablaSimbolos tabla = new TablaSimbolos();
        GeneradorCodigo gen = new GeneradorCodigo();
        Parser parser = new Parser(tokens, errores, tabla, gen);
        parser.parsearPrograma();

        List<TablaSimbolos.Simbolo> simbolos = tabla.obtenerTodos();

        ResultadoCompilacion resultado = new ResultadoCompilacion(
            tokens,
            simbolos,
            gen.getInstrucciones(),
            errores,
            errores.isEmpty()
        );

        return resultado;
    }
}
