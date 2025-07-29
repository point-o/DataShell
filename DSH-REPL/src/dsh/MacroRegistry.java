package dsh;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

public class MacroRegistry {
    private final Map<String, Macro> macros = new HashMap<>();
    
    /**
     * Defines a new macro in the registry.
     * If a macro with the same name already exists, it will be replaced.
     * 
     * @param macro The macro to define
     * @return true if this is a new macro, false if it replaced an existing one
     */
    public boolean define(Macro macro) {
        if (macro == null) {
            throw new IllegalArgumentException("Macro cannot be null");
        }
        
        boolean isNew = !macros.containsKey(macro.getName());
        macros.put(macro.getName(), macro);
        return isNew;
    }
    
    public boolean has(String name) {
        return name != null && macros.containsKey(name.trim());
    }
    
    /**
     * Gets a macro by name.
     * 
     * @param name The name of the macro to retrieve
     * @return The macro if found, null otherwise
     */
    public Macro get(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        return macros.get(name.trim());
    }
    
    /**
     * Checks if a macro with the given name exists.
     * 
     * @param name The name to check
     * @return true if a macro with this name exists
     */
    public boolean exists(String name) {
        return name != null && macros.containsKey(name.trim());
    }
    
    /**
     * Removes a macro from the registry.
     * 
     * @param name The name of the macro to remove
     * @return true if the macro was removed, false if it didn't exist
     */
    public boolean remove(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        return macros.remove(name.trim()) != null;
    }
    
    /**
     * Gets all macro names in the registry.
     * 
     * @return Unmodifiable set of macro names
     */
    public Set<String> getMacroNames() {
        return Collections.unmodifiableSet(macros.keySet());
    }
    
    /**
     * Gets the number of macros in the registry.
     * 
     * @return The count of registered macros
     */
    public int size() {
        return macros.size();
    }
    
    /**
     * Checks if the registry is empty.
     * 
     * @return true if no macros are registered
     */
    public boolean isEmpty() {
        return macros.isEmpty();
    }
    
    /**
     * Removes all macros from the registry.
     */
    public void clear() {
        macros.clear();
    }
    
    /**
     * Gets a string representation of all registered macros.
     * 
     * @return String listing all macro names and their line counts
     */
    @Override
    public String toString() {
        if (macros.isEmpty()) {
            return "MacroRegistry: empty";
        }
        
        StringBuilder sb = new StringBuilder("MacroRegistry (" + macros.size() + " macros):\n");
        for (Map.Entry<String, Macro> entry : macros.entrySet()) {
            Macro macro = entry.getValue();
            sb.append(String.format("  - %s (%d lines)%n", 
                entry.getKey(), 
                macro.getLineCount()));
        }
        return sb.toString();
    }
}