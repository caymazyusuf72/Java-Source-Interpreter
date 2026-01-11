package interpreter;

public class Value {
    public enum Type {
        INT, DOUBLE, BOOLEAN, STRING, OBJECT, NULL, VOID
    }
    
    private final Type type;
    private final Object value;
    
    public Value(Type type, Object value) {
        this.type = type;
        this.value = value;
    }
    
    public Type getType() {
        return type;
    }
    
    public Object getValue() {
        return value;
    }
    
    // Type conversion methods with error checking
    public int asInt() {
        if (type != Type.INT) {
            throw new RuntimeException("Type error: expected INT, got " + type);
        }
        return (Integer) value;
    }
    
    public double asDouble() {
        if (type == Type.INT) {
            return ((Integer) value).doubleValue();
        }
        if (type != Type.DOUBLE) {
            throw new RuntimeException("Type error: expected DOUBLE, got " + type);
        }
        return (Double) value;
    }
    
    public boolean asBoolean() {
        if (type != Type.BOOLEAN) {
            throw new RuntimeException("Type error: expected BOOLEAN, got " + type);
        }
        return (Boolean) value;
    }
    
    public String asString() {
        if (type == Type.NULL) return "null";
        if (type == Type.STRING) return (String) value;
        return value.toString();
    }
    
    public JavaObject asObject() {
        if (type != Type.OBJECT) {
            throw new RuntimeException("Type error: expected OBJECT, got " + type);
        }
        return (JavaObject) value;
    }
    
    // Arithmetic operations
    public Value add(Value other) {
        // String concatenation
        if (type == Type.STRING || other.type == Type.STRING) {
            return new Value(Type.STRING, asString() + other.asString());
        }
        
        // Numeric addition
        if (type == Type.DOUBLE || other.type == Type.DOUBLE) {
            return new Value(Type.DOUBLE, asDouble() + other.asDouble());
        }
        
        if (type == Type.INT && other.type == Type.INT) {
            return new Value(Type.INT, asInt() + other.asInt());
        }
        
        throw new RuntimeException("Cannot add " + type + " and " + other.type);
    }
    
    public Value subtract(Value other) {
        if (type == Type.DOUBLE || other.type == Type.DOUBLE) {
            return new Value(Type.DOUBLE, asDouble() - other.asDouble());
        }
        
        if (type == Type.INT && other.type == Type.INT) {
            return new Value(Type.INT, asInt() - other.asInt());
        }
        
        throw new RuntimeException("Cannot subtract " + type + " and " + other.type);
    }
    
    public Value multiply(Value other) {
        if (type == Type.DOUBLE || other.type == Type.DOUBLE) {
            return new Value(Type.DOUBLE, asDouble() * other.asDouble());
        }
        
        if (type == Type.INT && other.type == Type.INT) {
            return new Value(Type.INT, asInt() * other.asInt());
        }
        
        throw new RuntimeException("Cannot multiply " + type + " and " + other.type);
    }
    
    public Value divide(Value other) {
        if (type == Type.DOUBLE || other.type == Type.DOUBLE) {
            double divisor = other.asDouble();
            if (divisor == 0.0) throw new RuntimeException("Division by zero");
            return new Value(Type.DOUBLE, asDouble() / divisor);
        }
        
        if (type == Type.INT && other.type == Type.INT) {
            int divisor = other.asInt();
            if (divisor == 0) throw new RuntimeException("Division by zero");
            return new Value(Type.INT, asInt() / divisor);
        }
        
        throw new RuntimeException("Cannot divide " + type + " and " + other.type);
    }
    
    public Value modulo(Value other) {
        if (type == Type.INT && other.type == Type.INT) {
            int divisor = other.asInt();
            if (divisor == 0) throw new RuntimeException("Modulo by zero");
            return new Value(Type.INT, asInt() % divisor);
        }
        
        throw new RuntimeException("Cannot modulo " + type + " and " + other.type);
    }
    
    // Comparison operations
    public Value equals(Value other) {
        if (type == Type.NULL && other.type == Type.NULL) {
            return new Value(Type.BOOLEAN, true);
        }
        if (type == Type.NULL || other.type == Type.NULL) {
            return new Value(Type.BOOLEAN, false);
        }
        
        if (type != other.type) {
            return new Value(Type.BOOLEAN, false);
        }
        
        return new Value(Type.BOOLEAN, value.equals(other.value));
    }
    
    public Value notEquals(Value other) {
        return new Value(Type.BOOLEAN, !equals(other).asBoolean());
    }
    
    public Value lessThan(Value other) {
        if (type == Type.DOUBLE || other.type == Type.DOUBLE) {
            return new Value(Type.BOOLEAN, asDouble() < other.asDouble());
        }
        
        if (type == Type.INT && other.type == Type.INT) {
            return new Value(Type.BOOLEAN, asInt() < other.asInt());
        }
        
        throw new RuntimeException("Cannot compare " + type + " and " + other.type);
    }
    
    public Value lessOrEqual(Value other) {
        if (type == Type.DOUBLE || other.type == Type.DOUBLE) {
            return new Value(Type.BOOLEAN, asDouble() <= other.asDouble());
        }
        
        if (type == Type.INT && other.type == Type.INT) {
            return new Value(Type.BOOLEAN, asInt() <= other.asInt());
        }
        
        throw new RuntimeException("Cannot compare " + type + " and " + other.type);
    }
    
    public Value greaterThan(Value other) {
        if (type == Type.DOUBLE || other.type == Type.DOUBLE) {
            return new Value(Type.BOOLEAN, asDouble() > other.asDouble());
        }
        
        if (type == Type.INT && other.type == Type.INT) {
            return new Value(Type.BOOLEAN, asInt() > other.asInt());
        }
        
        throw new RuntimeException("Cannot compare " + type + " and " + other.type);
    }
    
    public Value greaterOrEqual(Value other) {
        if (type == Type.DOUBLE || other.type == Type.DOUBLE) {
            return new Value(Type.BOOLEAN, asDouble() >= other.asDouble());
        }
        
        if (type == Type.INT && other.type == Type.INT) {
            return new Value(Type.BOOLEAN, asInt() >= other.asInt());
        }
        
        throw new RuntimeException("Cannot compare " + type + " and " + other.type);
    }
    
    // Logical operations
    public Value and(Value other) {
        return new Value(Type.BOOLEAN, asBoolean() && other.asBoolean());
    }
    
    public Value or(Value other) {
        return new Value(Type.BOOLEAN, asBoolean() || other.asBoolean());
    }
    
    public Value not() {
        return new Value(Type.BOOLEAN, !asBoolean());
    }
    
    public Value negate() {
        if (type == Type.INT) {
            return new Value(Type.INT, -asInt());
        }
        if (type == Type.DOUBLE) {
            return new Value(Type.DOUBLE, -asDouble());
        }
        throw new RuntimeException("Cannot negate " + type);
    }
    
    @Override
    public String toString() {
        if (type == Type.NULL) return "null";
        if (type == Type.VOID) return "void";
        return value.toString();
    }
}