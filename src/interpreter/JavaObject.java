package interpreter;

import parser.ast.Declaration;
import java.util.HashMap;
import java.util.Map;

public class JavaObject {
    private final JavaClass klass;
    private final Map<String, Value> fields = new HashMap<>();
    
    public JavaObject(JavaClass klass) {
        this.klass = klass;
        
        // Initialize fields with default values
        for (Map.Entry<String, Declaration.Var> entry : klass.getFields().entrySet()) {
            String fieldName = entry.getKey();
            // Initialize with null/0/false based on type
            fields.put(fieldName, new Value(Value.Type.NULL, null));
        }
    }
    
    public JavaClass getJavaClass() {
        return klass;
    }
    
    public Value get(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }
        throw new RuntimeException("Undefined field: " + name);
    }
    
    public void set(String name, Value value) {
        if (fields.containsKey(name)) {
            fields.put(name, value);
            return;
        }
        // Allow setting new fields dynamically (for simplicity)
        fields.put(name, value);
    }
    
    @Override
    public String toString() {
        return "<instance of " + klass.getName() + ">";
    }
}