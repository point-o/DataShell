package dsh;

import java.util.List;

public class SimpleTester {
    
    public static void main(String[] args) {
        // Setup components
        Environment env = new Environment();
        MacroRegistry macros = new MacroRegistry();
        CommandRegistry commands = new CommandRegistry();
        TokenDispatcher dispatcher = new TokenDispatcher(env, macros, commands);
        Tokenizer tokenizer = new Tokenizer("");
        
        System.out.println("=== TokenDispatcher Simple Tester ===\n");
        
        // Test literals
        testCase("Literal String", "\"hello world\"", tokenizer, dispatcher);
        testCase("Literal Number", "42", tokenizer, dispatcher);
        testCase("Literal Boolean", "true", tokenizer, dispatcher);
        
        // Test assignment
        testCase("Assignment", "x = 100", tokenizer, dispatcher);
        testCase("String Assignment", "name = \"Ryan\"", tokenizer, dispatcher);
        
        // Test variable retrieval
        testCase("Variable Retrieval", "x", tokenizer, dispatcher);
        testCase("Variable Retrieval 2", "name", tokenizer, dispatcher);
        
        // Test undefined variable
        testCase("Undefined Variable", "undefined", tokenizer, dispatcher);
        
        // Test expressions (if Calculator exists)
        testCase("Expression", "#(2 + 3)", tokenizer, dispatcher);
        
        // Test unknown macro
        testCase("Unknown Macro", ";nonexistent", tokenizer, dispatcher);
        
        // Test unknown command
        testCase("Unknown Command", ":help", tokenizer, dispatcher);
        
        // Test empty input
        testCase("Empty Input", "", tokenizer, dispatcher);
        
        System.out.println("\n=== Test Summary ===");
        System.out.println("Environment state:");
        System.out.println("Variables: " + env.size());
        System.out.println("Macros: " + macros.size());
        System.out.println("Commands: " + commands.size());
    }
    
    private static void testCase(String description, String input, 
                                Tokenizer tokenizer, TokenDispatcher dispatcher) {
        System.out.println("Testing: " + description);
        System.out.println("Input: " + (input.isEmpty() ? "(empty)" : input));
        
        try {
            // Tokenize
            tokenizer.reset(input);
            Result<List<Token>> tokenResult = tokenizer.tokenize();
            
            if (tokenResult.isError()) {
                System.out.println("❌ Tokenization failed: " + tokenResult.getErrorMessage());
                System.out.println();
                return;
            }
            
            List<Token> tokens = tokenResult.getValue();
            if (tokens.isEmpty() && !input.isEmpty()) {
                System.out.println("❌ No tokens produced");
                System.out.println();
                return;
            }
            
            // Process tokens
            Result<Value> result = dispatcher.process(tokens);
            
            if (result.isOk()) {
                Value value = result.getValue();
                System.out.println("✅ Success: " + (value != null ? value.toString() : "null"));
            } else {
                System.out.println("❌ Error: " + result.getErrorMessage());
            }
            
        } catch (Exception e) {
            System.out.println("💥 Exception: " + e.getMessage());
        }
        
        System.out.println();
    }
}