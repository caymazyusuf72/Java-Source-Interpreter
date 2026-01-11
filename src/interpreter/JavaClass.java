package interpreter;

import parser.ast.Declaration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaClass {
    private final String name;
    private final Map<String, Declaration.Var> fields;
    private final Map<String, Declaration.Method> methods;
    
    public JavaClass(String name, List<Declaration.Var> fieldList, List<Declaration.Method> methodList) {
        this.name = name;
        this.fields = new HashMap<>();
        this.methods = new HashMap<>();
        
        for (Declaration.Var field : fieldList) {
            fields.put(field.name.lexeme, field);
        }
        
        for (Declaration.Method method : methodList) {
            methods.put(method.name.lexeme, method);
        }
    }
    
    public String getName() {
        return name;
    }
    
    public JavaObject instantiate() {
        return new JavaObject(this);
    }
    
    public Declaration.Method findMethod(String name) {
        return methods.get(name);
    }
    
    public Map<String, Declaration.Var> getFields() {
        return fields;
    }
    
    @Override
    public String toString() {
        return "<class " + name + ">";
    }
}