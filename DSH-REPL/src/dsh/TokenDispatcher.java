package dsh;

import java.util.List;
import java.util.ArrayList;

/**
 * Dispatches tokens for evaluation in the correct order.
 * Handles assignment vs output logic based on presence of = token.
 * 
 * @author Ryan Pointer
 * @version 7/29/25
 */
public class TokenDispatcher {
    private final Environment environment;
    private final MacroRegistry macroRegistry;
    private final CommandRegistry commandRegistry;
    
    public TokenDispatcher(Environment environment, MacroRegistry macroRegistry, CommandRegistry commandRegistry) {
        this.environment = environment;
        this.macroRegistry = macroRegistry;
        this.commandRegistry = commandRegistry;
    }
    
    /**
     * Process a list of tokens, handling assignment vs output logic.
     * If assignment token is present: variable = value
     * Otherwise: output the evaluated value
     */
    public Result<Value> process(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return Result.error(Result.ErrorType.SYNTAX, "No tokens to process");
        }
        
        // Check for assignment pattern: variable = value
        if (tokens.size() >= 3 && hasAssignmentPattern(tokens)) {
            return handleAssignment(tokens);
        } else {
            // No assignment, evaluate and output
            return handleOutput(tokens);
        }
    }
    
    private boolean hasAssignmentPattern(List<Token> tokens) {
        return tokens.size() >= 2 && 
               tokens.get(0).getType() == Token.TokenType.VARIABLE &&
               tokens.get(1).getType() == Token.TokenType.ASSIGNMENT;
    }
    
    private Result<Value> handleAssignment(List<Token> tokens) {
        Token variableToken = tokens.get(0);
        // Skip the assignment token at index 1
        List<Token> valueTokens = tokens.subList(2, tokens.size());
        
        if (valueTokens.isEmpty()) {
            return Result.error(Result.ErrorType.SYNTAX, 
                "Assignment requires a value after '='");
        }
        
        // Evaluate the value tokens
        Result<Value> valueResult = evaluateTokens(valueTokens);
        if (valueResult.isError()) {
            return valueResult;
        }
        
        // Store in environment
        String variableName = variableToken.getValue();
        environment.set(variableName, valueResult.getValue());
        
        // Return the assigned value
        return valueResult;
    }
    
    private Result<Value> handleOutput(List<Token> tokens) {
        // Evaluate all tokens and return the result for output
        return evaluateTokens(tokens);
    }
    
    /**
     * Evaluate tokens in the correct priority order:
     * 1. Literals (already parsed)
     * 2. Expressions
     * 3. Macros  
     * 4. Commands (with their parameters)
     * 5. Variables
     */
    private Result<Value> evaluateTokens(List<Token> tokens) {
        if (tokens.size() == 1) {
            return evaluateSingleToken(tokens.get(0));
        }
        
        // Check if the first token is a command - if so, group with parameters
        if (tokens.get(0).getType() == Token.TokenType.COMMAND) {
            return handleCommandWithParameters(tokens);
        }
        
        // For other multi-token scenarios, evaluate the last token
        // (This preserves existing behavior for non-command cases)
        Token lastToken = tokens.get(tokens.size() - 1);
        return evaluateSingleToken(lastToken);
    }
    
    /**
     * Handle a command token with its parameters
     */
    private Result<Value> handleCommandWithParameters(List<Token> tokens) {
        Token commandToken = tokens.get(0);
        List<Token> parameterTokens = tokens.subList(1, tokens.size());
        
        // Remove the : prefix and look up the command
        String commandName = commandToken.getValue().substring(1);
        
        if (!commandRegistry.hasCommand(commandName)) {
            return Result.error(Result.ErrorType.RUNTIME, 
                "Unknown command: " + commandName);
        }
        
        try {
            Command command = commandRegistry.getCommand(commandName);
            
            // Evaluate parameter tokens to get their values
            List<Value> parameters = new ArrayList<>();
            for (Token paramToken : parameterTokens) {
                Result<Value> paramResult = evaluateSingleToken(paramToken);
                if (paramResult.isError()) {
                    return Result.error(Result.ErrorType.RUNTIME,
                        String.format("Failed to evaluate parameter for command '%s': %s",
                            commandName, paramResult.getErrorMessage()));
                }
                parameters.add(paramResult.getValue());
            }
            
            // Convert list to array and execute command
            Value[] paramArray = parameters.toArray(new Value[0]);
            Value result = command.execute(environment, paramArray);
            return Result.ok(result);
            
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, 
                "Command execution failed: " + commandName + " - " + e.getMessage());
        }
    }
    
    private Result<Value> evaluateSingleToken(Token token) {
        return switch (token.getType()) {
            case LITERAL -> handleLiteral(token);
            case EXPRESSION -> handleExpression(token);
            case MACRO -> handleMacro(token);
            case COMMAND -> handleCommand(token);
            case VARIABLE -> handleVariable(token);
            case ASSIGNMENT -> Result.error(Result.ErrorType.SYNTAX, 
                "Unexpected assignment token");
        };
    }
    
    private Result<Value> handleLiteral(Token token) {
        // Literals already have their value parsed during tokenization
        Value literalValue = token.getLiteralValue();
        if (literalValue != null) {
            return Result.ok(literalValue);
        }
        return Result.error(Result.ErrorType.RUNTIME, 
            "Literal token missing value: " + token.getValue());
    }
    
    private Result<Value> handleExpression(Token token) {
        String expression = token.getValue().substring(1);
        Calculator calc = new Calculator(environment);
        return calc.evaluate(expression);
    }
    
    private Result<Value> handleMacro(Token token) {
        String macroName = token.getValue().substring(1);
        
        if (!macroRegistry.has(macroName)) {
            return Result.error(Result.ErrorType.RUNTIME, 
                "Unknown macro: " + macroName);
        }
        
        try {
            Macro macro = macroRegistry.get(macroName);
            return executeMacro(macro);
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, 
                "Macro execution failed: " + macroName + " - " + e.getMessage());
        }
    }
    
    /**
     * Execute a macro by processing each of its tokenized lines
     */
    private Result<Value> executeMacro(Macro macro) {
        List<List<Token>> tokenLines = macro.getTokenLines();
        
        if (tokenLines.isEmpty()) {
            return Result.error(Result.ErrorType.RUNTIME, 
                "Macro '" + macro.getName() + "' is empty");
        }
        
        Value lastResult = null;
        
        // Execute each line of the macro
        for (int lineIndex = 0; lineIndex < tokenLines.size(); lineIndex++) {
            List<Token> tokens = tokenLines.get(lineIndex);
            
            if (tokens.isEmpty()) {
                continue; // Skip empty lines (from tokenization errors)
            }
            
            Result<Value> lineResult = process(tokens);
            if (lineResult.isError()) {
                return Result.error(Result.ErrorType.RUNTIME,
                    String.format("Macro '%s' failed at line %d: %s",
                        macro.getName(), lineIndex + 1, lineResult.getErrorMessage()));
            }
            
            lastResult = lineResult.getValue();
        }
        
        // Return the result of the last executed line
        return Result.ok(lastResult != null ? lastResult : new AString(""));
    }
    
    private Result<Value> handleCommand(Token token) {
        // Handle single command without parameters (backwards compatibility)
        String commandName = token.getValue().substring(1);
        
        if (!commandRegistry.hasCommand(commandName)) {
            return Result.error(Result.ErrorType.RUNTIME, 
                "Unknown command: " + commandName);
        }
        
        try {
            Command command = commandRegistry.getCommand(commandName);
            Value result = command.execute(environment); // No parameters
            return Result.ok(result);
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, 
                "Command execution failed: " + commandName + " - " + e.getMessage());
        }
    }
    
    private Result<Value> handleVariable(Token token) {
        String variableName = token.getValue();
        
        if (!environment.has(variableName)) {
            return Result.error(Result.ErrorType.RUNTIME, 
                "Undefined variable: " + variableName);
        }
        
        Value value = environment.get(variableName);
        return Result.ok(value);
    }
}