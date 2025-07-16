package dsh;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to hold commands
 * 
 * @author Ryan Pointer
 * @version 7/16/25
 */
public class CommandRegistry {
    private Map<String, Command> commands;
    
    public CommandRegistry() {
        this.commands = new HashMap<>();
        initializeCommands();
    }
    
    private void initializeCommands() {
        
    }
    
    public void registerCommand(String name, Command command) {
        commands.put(name, command);
    }
    
    public Command getCommand(String name) {
        return commands.get(name);
    }
    
    public boolean hasCommand(String name) {
        return commands.containsKey(name);
    }
    
    public java.util.Set<String> getCommandNames() {
        return commands.keySet();
    }
    
    public java.util.Map<String, Command> getAllCommands() {
        return new java.util.HashMap<>(commands);
    }
}