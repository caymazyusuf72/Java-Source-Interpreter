import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import parser.ast.Declaration;
import interpreter.Interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main <source-file>");
            System.err.println("Example: java Main examples/simple.java");
            System.exit(1);
        }
        
        String sourceFile = args[0];
        
        try {
            // Read source file
            String source = readFile(sourceFile);
            
            System.out.println("=== Java Source Interpreter ===");
            System.out.println("Interpreting: " + sourceFile);
            System.out.println("===============================\n");
            
            // Lexical Analysis
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();
            System.out.println("[Lexer] Generated " + tokens.size() + " tokens");
            
            // Syntax Analysis
            Parser parser = new Parser(tokens);
            List<Declaration> declarations = parser.parse();
            System.out.println("[Parser] Parsed " + declarations.size() + " declarations");
            
            // Interpretation
            System.out.println("[Interpreter] Starting execution...\n");
            System.out.println("--- Output ---");
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(declarations);
            System.out.println("\n--- End of Output ---");
            
            System.out.println("\n[Complete] Program executed successfully");
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static String readFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes);
    }
}