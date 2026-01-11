package parser.ast;

import lexer.Token;
import interpreter.Visitor;
import java.util.List;

public abstract class Expression implements ASTNode {
    
    // Binary expression: a + b, x == y
    public static class Binary extends Expression {
        public final Expression left;
        public final Token operator;
        public final Expression right;
        
        public Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }
    
    // Literal: 42, "hello", true
    public static class Literal extends Expression {
        public final Object value;
        
        public Literal(Object value) {
            this.value = value;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }
    
    // Variable: x, myVar
    public static class Variable extends Expression {
        public final Token name;
        
        public Variable(Token name) {
            this.name = name;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }
    
    // Assignment: x = 5
    public static class Assign extends Expression {
        public final Token name;
        public final Expression value;
        
        public Assign(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }
    
    // Unary: !x, -y
    public static class Unary extends Expression {
        public final Token operator;
        public final Expression right;
        
        public Unary(Token operator, Expression right) {
            this.operator = operator;
            this.right = right;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }
    
    // Call: foo(1, 2, 3)
    public static class Call extends Expression {
        public final Expression callee;
        public final Token paren;
        public final List<Expression> arguments;
        
        public Call(Expression callee, Token paren, List<Expression> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }
    
    // Get: object.field
    public static class Get extends Expression {
        public final Expression object;
        public final Token name;
        
        public Get(Expression object, Token name) {
            this.object = object;
            this.name = name;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }
    
    // Set: object.field = value
    public static class Set extends Expression {
        public final Expression object;
        public final Token name;
        public final Expression value;
        
        public Set(Expression object, Token name, Expression value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }
    
    // New: new Calculator()
    public static class New extends Expression {
        public final Token className;
        public final List<Expression> arguments;
        
        public New(Token className, List<Expression> arguments) {
            this.className = className;
            this.arguments = arguments;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitNewExpr(this);
        }
    }
    
    // This: this
    public static class This extends Expression {
        public final Token keyword;
        
        public This(Token keyword) {
            this.keyword = keyword;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
        }
    }
}