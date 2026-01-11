package parser.ast;

import lexer.Token;
import interpreter.Visitor;
import java.util.List;

public abstract class Declaration implements ASTNode {
    
    // Parameter for methods
    public static class Parameter {
        public final Token type;
        public final Token name;
        
        public Parameter(Token type, Token name) {
            this.type = type;
            this.name = name;
        }
    }
    
    // Variable: int x = 5;
    public static class Var extends Declaration {
        public final Token type;
        public final Token name;
        public final Expression initializer;
        
        public Var(Token type, Token name, Expression initializer) {
            this.type = type;
            this.name = name;
            this.initializer = initializer;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDecl(this);
        }
    }
    
    // Method: int add(int a, int b) { ... }
    public static class Method extends Declaration {
        public final Token returnType;
        public final Token name;
        public final List<Parameter> parameters;
        public final Statement.Block body;
        
        public Method(Token returnType, Token name, List<Parameter> parameters, Statement.Block body) {
            this.returnType = returnType;
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitMethodDecl(this);
        }
    }
    
    // Class: class Calculator { ... }
    public static class Class extends Declaration {
        public final Token name;
        public final List<Var> fields;
        public final List<Method> methods;
        
        public Class(Token name, List<Var> fields, List<Method> methods) {
            this.name = name;
            this.fields = fields;
            this.methods = methods;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassDecl(this);
        }
    }
}