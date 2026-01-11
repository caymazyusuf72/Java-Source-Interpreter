package interpreter;

import lexer.Token;
import lexer.TokenType;
import parser.ast.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Visitor<Value> {
    private final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<String, JavaClass> classes = new HashMap<>();
    private JavaObject currentInstance = null;
    
    // Return value exception for control flow
    private static class ReturnException extends RuntimeException {
        final Value value;
        ReturnException(Value value) {
            super(null, null, false, false);
            this.value = value;
        }
    }
    
    public void interpret(List<Declaration> declarations) {
        try {
            // First pass: register all classes
            for (Declaration declaration : declarations) {
                if (declaration instanceof Declaration.Class) {
                    declaration.accept(this);
                }
            }
            
