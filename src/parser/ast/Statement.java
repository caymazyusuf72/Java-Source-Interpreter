package parser.ast;

import lexer.Token;
import interpreter.Visitor;
import java.util.List;

public abstract class Statement implements ASTNode {
    
    // Expression statement: println(x);
    public static class ExpressionStmt extends Statement {
        public final Expression expression;
        
        public ExpressionStmt(Expression expression) {
            this.expression = expression;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }
    
    // Block: { stmt1; stmt2; }
    public static class Block extends Statement {
        public final List<Statement> statements;
        
        public Block(List<Statement> statements) {
            this.statements = statements;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }
    
    // If: if (condition) thenBranch else elseBranch
    public static class If extends Statement {
        public final Expression condition;
        public final Statement thenBranch;
        public final Statement elseBranch;
        
        public If(Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }
    
    // While: while (condition) body
    public static class While extends Statement {
        public final Expression condition;
        public final Statement body;
        
        public While(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }
    
    // For: for (init; condition; increment) body
    public static class For extends Statement {
        public final Statement initializer;
        public final Expression condition;
        public final Expression increment;
        public final Statement body;
        
        public For(Statement initializer, Expression condition, Expression increment, Statement body) {
            this.initializer = initializer;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }
    }
    
    // Return: return value;
    public static class Return extends Statement {
        public final Token keyword;
        public final Expression value;
        
        public Return(Token keyword, Expression value) {
            this.keyword = keyword;
            this.value = value;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }
}