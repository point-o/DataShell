package dsh;

import java.util.HashMap;
import java.util.Map;

/**
 * An enviornment to store variables
 * 
 * @author Ryan Pointer
 * @version 7/9/25
 */
public class Environment {
    private final Map<String, Value> variables = new HashMap<>();
    
    public Value get(String name) {
        return variables.getOrDefault(name, new ANull());
    }
    
    public void set(String name, Value value) {
        variables.put(name, value);
    }
    
    public boolean has(String name) {
        return variables.containsKey(name);
    }
}