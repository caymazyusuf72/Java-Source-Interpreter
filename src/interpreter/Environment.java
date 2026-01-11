package interpreter;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Value> values = new HashMap<>();
    private final Environment parent;
    
    // Global environment
    public Environment() {
        this.parent = null;
    }
    
    // Nested environment (for scopes)
    public Environment(Environment parent) {
        this.parent = parent;
    }
    
    // Define a new variable in this scope
    public void define(String name, Value value) {
        values.put(name, value);
    }
    
    // Get a variable value (searches up the scope chain)
    public Value get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        
        if (parent != null) {
            return parent.get(name);
        }
        
        throw new RuntimeException("Undefined variable: " + name);
    }
    
    // Assign to an existing variable (searches up the scope chain)
    public void assign(String name, Value value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }
        
        if (parent != null) {
            parent.assign(name, value);
            return;
        }
        
        throw new RuntimeException("Undefined variable: " + name);
    }
    
    // Create a child environment
    public Environment createChild() {
        return new Environment(this);
    }
    
    // Check if variable exists in this scope only
    public boolean hasInCurrentScope(String name) {
        return values.containsKey(name);
    }
}