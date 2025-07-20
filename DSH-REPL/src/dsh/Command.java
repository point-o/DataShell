package dsh;

/**
 * A class for Commands
 * @author Ryan Pointer
 * @version 7/16/25
 */
@FunctionalInterface
interface CommandFunction {
    Value execute(Environment context, Value... args); // ... is variable length, so 0 to some number of Value args are allowed
}

class Command {
    private String description;
    private String hint;
    private CommandFunction function;
	protected int params;
    
    public Command(String description, String hint, CommandFunction function, int params) {
        this.description = description;
        this.hint = hint;
        this.function = function;
        this.params = params;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getHint() {
        return hint;
    }
    
    public Value execute(Environment context, Value... args) {
        return function.execute(context, args);
    }
}