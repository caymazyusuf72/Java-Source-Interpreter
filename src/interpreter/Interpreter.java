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
            
            // Find and execute Main.main()
            JavaClass mainClass = classes.get("Main");
            if (mainClass == null) {
                throw new RuntimeException("No Main class found");
            }
            
            Declaration.Method mainMethod = mainClass.findMethod("main");
            if (mainMethod == null) {
                throw new RuntimeException("No main() method found in Main class");
            }
            
            // Create Main instance and call main()
            JavaObject mainInstance = mainClass.instantiate();
            currentInstance = mainInstance;
            executeBlock(mainMethod.body.statements, environment);
            
        } catch (ReturnException e) {
            // Main completed with return
        } catch (RuntimeException e) {
            System.err.println("Runtime Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ========== Expression Visitors ==========
    
    @Override
    public Value visitBinaryExpr(Expression.Binary expr) {
        Value left = evaluate(expr.left);
        Value right = evaluate(expr.right);
        
        switch (expr.operator.type) {
            case PLUS:
                return left.add(right);
            case MINUS:
                return left.subtract(right);
            case STAR:
                return left.multiply(right);
            case SLASH:
                return left.divide(right);
            case PERCENT:
                return left.modulo(right);
            case EQUAL_EQUAL:
                return left.equals(right);
            case BANG_EQUAL:
                return left.notEquals(right);
            case LESS:
                return left.lessThan(right);
            case LESS_EQUAL:
                return left.lessOrEqual(right);
            case GREATER:
                return left.greaterThan(right);
            case GREATER_EQUAL:
                return left.greaterOrEqual(right);
            case AND:
                return left.and(right);
            case OR:
                return left.or(right);
            default:
                throw new RuntimeException("Unknown binary operator: " + expr.operator.lexeme);
        }
    }
    
    @Override
    public Value visitLiteralExpr(Expression.Literal expr) {
        if (expr.value == null) {
            return new Value(Value.Type.NULL, null);
        }
        
        if (expr.value instanceof Integer) {
            return new Value(Value.Type.INT, expr.value);
        }
        if (expr.value instanceof Double) {
            return new Value(Value.Type.DOUBLE, expr.value);
        }
        if (expr.value instanceof Boolean) {
            return new Value(Value.Type.BOOLEAN, expr.value);
        }
        if (expr.value instanceof String) {
            return new Value(Value.Type.STRING, expr.value);
        }
        
        throw new RuntimeException("Unknown literal type: " + expr.value.getClass());
    }
    
    @Override
    public Value visitVariableExpr(Expression.Variable expr) {
        return environment.get(expr.name.lexeme);
    }
    
    @Override
    public Value visitAssignExpr(Expression.Assign expr) {
        Value value = evaluate(expr.value);
        environment.assign(expr.name.lexeme, value);
        return value;
    }
    
    @Override
    public Value visitUnaryExpr(Expression.Unary expr) {
        Value right = evaluate(expr.right);
        
        switch (expr.operator.type) {
            case MINUS:
                return right.negate();
            case BANG:
                return right.not();
            default:
                throw new RuntimeException("Unknown unary operator: " + expr.operator.lexeme);
        }
    }
    
    @Override
    public Value visitCallExpr(Expression.Call expr) {
        // Check for System.out.println special case
        if (isSystemOutPrintln(expr)) {
            if (expr.arguments.size() != 1) {
                throw new RuntimeException("println expects 1 argument");
            }
            Value arg = evaluate(expr.arguments.get(0));
            System.out.println(arg.toString());
            return new Value(Value.Type.VOID, null);
        }
        
        // If callee is a Get expression, it's a method call on an object
        if (expr.callee instanceof Expression.Get) {
            Expression.Get get = (Expression.Get) expr.callee;
            Value objectValue = evaluate(get.object);
            
            if (objectValue.getType() != Value.Type.OBJECT) {
                throw new RuntimeException("Cannot call method on non-object");
            }
            
            JavaObject object = objectValue.asObject();
            JavaClass klass = object.getJavaClass();
            Declaration.Method method = klass.findMethod(get.name.lexeme);
            
            if (method == null) {
                throw new RuntimeException("Undefined method: " + get.name.lexeme);
            }
            
            return callMethod(method, expr.arguments, object);
        }
        
        throw new RuntimeException("Can only call methods");
    }
    
    private boolean isSystemOutPrintln(Expression.Call expr) {
        if (!(expr.callee instanceof Expression.Get)) return false;
        Expression.Get printlnGet = (Expression.Get) expr.callee;
        
        if (!printlnGet.name.lexeme.equals("println")) return false;
        if (!(printlnGet.object instanceof Expression.Get)) return false;
        
        Expression.Get outGet = (Expression.Get) printlnGet.object;
        if (!outGet.name.lexeme.equals("out")) return false;
        if (!(outGet.object instanceof Expression.Variable)) return false;
        
        Expression.Variable systemVar = (Expression.Variable) outGet.object;
        return systemVar.name.lexeme.equals("System");
    }
    
    private Value callMethod(Declaration.Method method, List<Expression> arguments, JavaObject instance) {
        // Check parameter count
        if (arguments.size() != method.parameters.size()) {
            throw new RuntimeException("Expected " + method.parameters.size() + 
                " arguments but got " + arguments.size());
        }
        
        // Create new environment for method
        Environment previous = this.environment;
        JavaObject previousInstance = this.currentInstance;
        
        try {
            this.environment = new Environment(globals);
            this.currentInstance = instance;
            
            // Bind this
            this.environment.define("this", new Value(Value.Type.OBJECT, instance));
            
            // Bind parameters
            for (int i = 0; i < method.parameters.size(); i++) {
                String paramName = method.parameters.get(i).name.lexeme;
                Value argValue = evaluate(arguments.get(i));
                this.environment.define(paramName, argValue);
            }
            
            // Execute method body
            try {
                executeBlock(method.body.statements, this.environment);
            } catch (ReturnException returnValue) {
                return returnValue.value;
            }
            
            // No explicit return - return void or default value
            if (method.returnType.type == TokenType.VOID) {
                return new Value(Value.Type.VOID, null);
            } else if (method.returnType.type == TokenType.INT) {
                return new Value(Value.Type.INT, 0);
            } else if (method.returnType.type == TokenType.DOUBLE) {
                return new Value(Value.Type.DOUBLE, 0.0);
            } else if (method.returnType.type == TokenType.BOOLEAN) {
                return new Value(Value.Type.BOOLEAN, false);
            } else {
                return new Value(Value.Type.NULL, null);
            }
            
        } finally {
            this.environment = previous;
            this.currentInstance = previousInstance;
        }
    }
    
    @Override
    public Value visitGetExpr(Expression.Get expr) {
        Value object = evaluate(expr.object);
        
        if (object.getType() != Value.Type.OBJECT) {
            throw new RuntimeException("Only objects have fields");
        }
        
        return object.asObject().get(expr.name.lexeme);
    }
    
    @Override
    public Value visitSetExpr(Expression.Set expr) {
        Value object = evaluate(expr.object);
        
        if (object.getType() != Value.Type.OBJECT) {
            throw new RuntimeException("Only objects have fields");
        }
        
        Value value = evaluate(expr.value);
        object.asObject().set(expr.name.lexeme, value);
        return value;
    }
    
    @Override
    public Value visitNewExpr(Expression.New expr) {
        String className = expr.className.lexeme;
        JavaClass klass = classes.get(className);
        
        if (klass == null) {
            throw new RuntimeException("Undefined class: " + className);
        }
        
        JavaObject instance = klass.instantiate();
        
        // Initialize fields with their initializers
        for (Map.Entry<String, Declaration.Var> entry : klass.getFields().entrySet()) {
            Declaration.Var field = entry.getValue();
            if (field.initializer != null) {
                JavaObject previousInstance = this.currentInstance;
                this.currentInstance = instance;
                Value initValue = evaluate(field.initializer);
                instance.set(field.name.lexeme, initValue);
                this.currentInstance = previousInstance;
            }
        }
        
        return new Value(Value.Type.OBJECT, instance);
    }
    
    @Override
    public Value visitThisExpr(Expression.This expr) {
        if (currentInstance == null) {
            throw new RuntimeException("Cannot use 'this' outside of a class");
        }
        return new Value(Value.Type.OBJECT, currentInstance);
    }
    
    // ========== Statement Visitors ==========
    
    @Override
    public Value visitExpressionStmt(Statement.ExpressionStmt stmt) {
        evaluate(stmt.expression);
        return null;
    }
    
    @Override
    public Value visitBlockStmt(Statement.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }
    
    private void executeBlock(List<Statement> statements, Environment blockEnv) {
        Environment previous = this.environment;
        try {
            this.environment = blockEnv;
            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }
    
    @Override
    public Value visitIfStmt(Statement.If stmt) {
        Value condition = evaluate(stmt.condition);
        
        if (condition.asBoolean()) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        
        return null;
    }
    
    @Override
    public Value visitWhileStmt(Statement.While stmt) {
        while (evaluate(stmt.condition).asBoolean()) {
            execute(stmt.body);
        }
        return null;
    }
    
    @Override
    public Value visitForStmt(Statement.For stmt) {
        // Execute initializer in new scope
        Environment previous = this.environment;
        try {
            this.environment = new Environment(environment);
            
            if (stmt.initializer != null) {
                execute(stmt.initializer);
            }
            
            while (stmt.condition == null || evaluate(stmt.condition).asBoolean()) {
                execute(stmt.body);
                
                if (stmt.increment != null) {
                    evaluate(stmt.increment);
                }
            }
        } finally {
            this.environment = previous;
        }
        
        return null;
    }
    
    @Override
    public Value visitReturnStmt(Statement.Return stmt) {
        Value value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        } else {
            value = new Value(Value.Type.VOID, null);
        }
        
        throw new ReturnException(value);
    }
    
    // ========== Declaration Visitors ==========
    
    @Override
    public Value visitVarDecl(Declaration.Var decl) {
        Value value = new Value(Value.Type.NULL, null);
        
        if (decl.initializer != null) {
            value = evaluate(decl.initializer);
        } else {
            // Default initialization based on type
            if (decl.type.type == TokenType.INT) {
                value = new Value(Value.Type.INT, 0);
            } else if (decl.type.type == TokenType.DOUBLE) {
                value = new Value(Value.Type.DOUBLE, 0.0);
            } else if (decl.type.type == TokenType.BOOLEAN) {
                value = new Value(Value.Type.BOOLEAN, false);
            }
        }
        
        environment.define(decl.name.lexeme, value);
        return null;
    }
    
    @Override
    public Value visitMethodDecl(Declaration.Method decl) {
        // Methods are stored in their class, not executed directly
        return null;
    }
    
    @Override
    public Value visitClassDecl(Declaration.Class decl) {
        JavaClass klass = new JavaClass(decl.name.lexeme, decl.fields, decl.methods);
        classes.put(decl.name.lexeme, klass);
        return null;
    }
    
    // ========== Helper Methods ==========
    
    private Value evaluate(Expression expr) {
        return expr.accept(this);
    }
    
    private void execute(Statement stmt) {
        stmt.accept(this);
    }
}