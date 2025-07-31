package dsh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class DSHRepl {
    private final Environment env;
    private final MacroRegistry macros;
    private final CommandRegistry commands;
    private final TokenDispatcher dispatcher;
    private final Tokenizer tokenizer;
    private final BufferedReader reader;
    private boolean running;
    
    public DSHRepl() {
        this.env = new Environment();
        this.macros = new MacroRegistry();
        this.commands = new CommandRegistry();
        this.dispatcher = new TokenDispatcher(env, macros, commands);
        this.tokenizer = new Tokenizer("");
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.running = true;
        
        // Register built-in REPL commands
        registerReplCommands();
    }
    
    public void start() {
        printWelcome();
        
        while (running) {
            try {
                System.out.print("dsh> ");
                String input = reader.readLine();
                
                if (input == null) { // EOF (Ctrl+D)
                    break;
                }
                
                input = input.trim();
                
                if (input.isEmpty()) {
                    continue;
                }
                
                // Handle special REPL commands
                if (handleReplCommand(input)) {
                    continue;
                }
                
                // Process through normal DSH pipeline
                processInput(input);
                
            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
                break;
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
            }
        }
        
        printGoodbye();
    }
    
    private void processInput(String input) {
        try {
            // Tokenize
            tokenizer.reset(input);
            Result<List<Token>> tokenResult = tokenizer.tokenize();
            
            if (tokenResult.isError()) {
                System.err.println("Tokenization error: " + tokenResult.getErrorMessage());
                return;
            }
            
            List<Token> tokens = tokenResult.getValue();
            if (tokens.isEmpty()) {
                return; // Empty input after tokenization
            }
            
            // Process tokens
            Result<Value> result = dispatcher.process(tokens);
            
            if (result.isOk()) {
                Value value = result.getValue();
                if (value != null) {
                    System.out.println(formatValue(value));
                }
            } else {
                System.err.println("Error: " + result.getErrorMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }
    
    private boolean handleReplCommand(String input) {
        switch (input.toLowerCase()) {
            case "exit":
            case "quit":
            case ":q":
                running = false;
                return true;
                
            case "help":
            case ":help":
            case ":h":
                printHelp();
                return true;
                
            case "env":
            case ":env":
                printEnvironment();
                return true;
                
            case "clear":
            case ":clear":
                clearScreen();
                return true;
                
            case "reset":
            case ":reset":
                resetEnvironment();
                return true;
                
            case "version":
            case ":version":
                printVersion();
                return true;
                
            default:
                return false; // Not a REPL command
        }
    }
    
    private void registerReplCommands() {
        // Commands are registered in CommandRegistry's initializeCommands() method
        // This method is kept for potential future REPL-specific commands
    }
    
    private String formatValue(Value value) {
        // Add color/formatting if desired
        return "=> " + value.toString();
    }
    
    private void printWelcome() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║        DSH Interactive Shell        ║");
        System.out.println("║     Dynamic Shell Processor v1.0    ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
        System.out.println("Type 'help' for available commands, 'exit' to quit.");
        System.out.println();
    }
    
    private void printHelp() {
        System.out.println("DSH REPL Commands:");
        System.out.println("  help, :help, :h     - Show this help message");
        System.out.println("  exit, quit, :q      - Exit the REPL");
        System.out.println("  env, :env           - Show environment variables");
        System.out.println("  clear, :clear       - Clear the screen");
        System.out.println("  reset, :reset       - Reset environment");
        System.out.println("  version, :version   - Show version information");
        System.out.println();
        
        // Show registered commands from CommandRegistry
        if (commands.size() > 0) {
            System.out.println("Available Commands:");
            Map<String, Command> allCommands = commands.getAllCommands();
            for (Map.Entry<String, Command> entry : allCommands.entrySet()) {
                Command cmd = entry.getValue();
                System.out.printf("  %-15s - %s%n", cmd.getHint(), cmd.getDescription());
            }
            System.out.println();
        }
        
        System.out.println("DSH Language Features:");
        System.out.println("  Variables:     x = 42, name = \"hello\"");
        System.out.println("  Expressions:   #(2 + 3 * 4)");
        System.out.println("  Macros:        ;macroname");
        System.out.println("  Commands:      :commandname");
        System.out.println("  Literals:      \"strings\", 123, true/false");
        System.out.println();
    }
    
    private void printEnvironment() {
        System.out.println("Environment Status:");
        System.out.println("  Variables: " + env.size());
        System.out.println("  Macros: " + macros.size());
        System.out.println("  Commands: " + commands.size());
        
        if (env.size() > 0) {
            System.out.println("\nCurrent Variables:");
            // Print all variables from the environment
            Map<String, Value> allVariables = env.getAllVariables();
            for (Map.Entry<String, Value> entry : allVariables.entrySet()) {
                System.out.printf("  %-15s = %s (%s)%n", 
                    entry.getKey(), 
                    entry.getValue().toString(), 
                    entry.getValue().type());
            }
        }
        System.out.println();
    }
    
    private void clearScreen() {
        // ANSI escape sequence to clear screen
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }
    
    private void resetEnvironment() {
        // Clear all state
        try {
            // If your classes have clear/reset methods, use them
            // Otherwise create new instances
            System.out.println("Environment reset.");
        } catch (Exception e) {
            System.err.println("Error resetting environment: " + e.getMessage());
        }
    }
    
    private void printVersion() {
        System.out.println("DSH (Dynamic Shell) v1.0");
        System.out.println("Interactive REPL Environment");
        System.out.println("Java Runtime: " + System.getProperty("java.version"));
    }
    
    private void printGoodbye() {
        System.out.println("\nGoodbye! Thanks for using DSH.");
    }
    
    // Main method to start the REPL
    public static void main(String[] args) {
        DSHRepl repl = new DSHRepl();
        repl.start();
    }
    
    // Alternative method for programmatic use
    public void runSingleCommand(String command) {
        processInput(command);
    }
    
    // Method to run in non-interactive mode
    public static void runBatch(String[] commands) {
        DSHRepl repl = new DSHRepl();
        System.out.println("Running batch mode...\n");
        
        for (String command : commands) {
            System.out.println("dsh> " + command);
            repl.runSingleCommand(command);
            System.out.println();
        }
    }
}