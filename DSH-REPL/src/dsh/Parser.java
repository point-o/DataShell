package dsh;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase 1 Parser: Converts tokens to AST structure with graceful error handling
 * Does NOT evaluate expressions - just builds structure
 * 
 * @author Ryan Pointer
 * @version 7/20/25
 */
public class Parser {
    private List<Token> tokens;
    private int current = 0;
    
    public Parser() {
        this.tokens = new ArrayList<>();
    }
    
    /**
     * Parse tokens into an Input AST node with graceful error recovery
     */
    public Result<Input> parse(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
        
        // Handle empty or null token lists gracefully
        if (tokens == null || tokens.isEmpty()) {
            return Result.error(Result.ErrorType.SYNTAX, "No input provided");
        }
        
        try {
            Statement statement = parseStatement();
            return Result.ok(new Input(statement));
        } catch (ParseException e) {
            return Result.error(Result.ErrorType.SYNTAX, e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            return Result.error(Result.ErrorType.SYNTAX, "Unexpected end of input");
        } catch (Exception e) {
            // Catch any other unexpected errors
            return Result.error(Result.ErrorType.SYNTAX, "Parse error: " + e.getMessage());
        }
    }
    
    private Statement parseStatement() throws ParseException {
        // Handle completely empty input or only EOF
        if (isAtEnd() || (tokens.size() == 1 && check(TokenType.EOF))) {
            // For REPL, empty input should be allowed - return a no-op expression
            return new Expression("");
        }
        
        // Command: :name args...
        if (check(TokenType.COMMAND)) {
            return parseCommand();
        }
        
        // Macro: ;name args...
        if (check(TokenType.MACRO)) {
            return parseMacro();
        }
        
        // Assignment: identifier = expression  
        if (checkAssignment()) {
            return parseAssignment();
        }
        
        // Default: treat as expression (includes if, while, etc.)
        return parseExpression();
    }
    
    private Command parseCommand() throws ParseException {
        Token commandToken = consume(TokenType.COMMAND, "Expected command");
        
        // Safely extract command name
        String lexeme = commandToken.getLexeme();
        if (lexeme == null || lexeme.length() < 2 || !lexeme.startsWith(":")) {
            throw new ParseException("Invalid command format");
        }
        String commandName = lexeme.substring(1);
        
        // Validate command name is not empty
        if (commandName.trim().isEmpty()) {
            throw new ParseException("Command name cannot be empty");
        }
        
        List<String> args = collectRemainingAsStrings();
        return new Command(commandName, args);
    }
    
    private Macro parseMacro() throws ParseException {
        Token macroToken = consume(TokenType.MACRO, "Expected macro");
        
        // Safely extract macro name
        String lexeme = macroToken.getLexeme();
        if (lexeme == null || lexeme.length() < 2 || !lexeme.startsWith(";")) {
            throw new ParseException("Invalid macro format");
        }
        String macroName = lexeme.substring(1);
        
        // Validate macro name is not empty
        if (macroName.trim().isEmpty()) {
            throw new ParseException("Macro name cannot be empty");
        }
        
        List<String> args = collectRemainingAsStrings();
        return new Macro(macroName, args);
    }
    
    private Assignment parseAssignment() throws ParseException {
        Token identifier = consume(TokenType.IDENTIFIER, "Expected identifier");
        consume(TokenType.ASSIGN, "Expected '='");
        
        String expression = collectRemainingAsString();
        
        // Validate identifier
        String identifierName = identifier.getLexeme();
        if (identifierName == null || identifierName.trim().isEmpty()) {
            throw new ParseException("Variable name cannot be empty");
        }
        
        return new Assignment(identifierName, expression);
    }
    
    private Expression parseExpression() throws ParseException {
        String expression = collectRemainingAsString();
        
        // Allow empty expressions in REPL context
        if (expression.isEmpty()) {
            return new Expression("");
        }
        
        return new Expression(expression);
    }
    
    private boolean checkAssignment() {
        if (!check(TokenType.IDENTIFIER)) return false;
        
        // Safely look ahead for '='
        try {
            if (current + 1 < tokens.size()) {
                return tokens.get(current + 1).getType() == TokenType.ASSIGN;
            }
        } catch (Exception e) {
            // If anything goes wrong with lookahead, assume it's not an assignment
            return false;
        }
        
        return false;
    }
    
    private List<String> collectRemainingAsStrings() {
        List<String> args = new ArrayList<>();
        
        while (!isAtEnd() && !check(TokenType.EOF)) {
            Token token = advance();
            if (token != null && token.getLexeme() != null) {
                args.add(token.getLexeme());
            }
        }
        
        return args;
    }
    
    private String collectRemainingAsString() throws ParseException {
        StringBuilder expression = new StringBuilder();
        boolean first = true;
        
        while (!isAtEnd() && !check(TokenType.EOF)) {
            Token token = advance();
            if (token != null && token.getLexeme() != null) {
                if (!first) {
                    expression.append(" ");
                }
                expression.append(token.getLexeme());
                first = false;
            }
        }
        
        return expression.toString().trim();
    }
    
    // Token navigation helpers with bounds checking
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        Token token = peek();
        return token != null && token.getType() == type;
    }
    
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    
    private boolean isAtEnd() {
        return current >= tokens.size() || 
               (current < tokens.size() && peek() != null && peek().getType() == TokenType.EOF);
    }
    
    private Token peek() {
        if (current >= tokens.size()) {
            // Return a dummy EOF token if we're past the end
            return Token.eof(0, 0);
        }
        return tokens.get(current);
    }
    
    private Token previous() {
        if (current > 0 && current - 1 < tokens.size()) {
            return tokens.get(current - 1);
        }
        // Return dummy token if bounds are invalid
        return Token.eof(0, 0);
    }
    
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    
    private Token consume(TokenType type, String message) throws ParseException {
        if (check(type)) return advance();
        
        Token current = peek();
        if (current == null) {
            throw new ParseException(message + " - unexpected end of input");
        }
        
        throw new ParseException(message + " at line " + current.getLine() + 
                               ", column " + current.getColumn() + 
                               ". Got: " + current.getType());
    }
}

/**
 * Exception thrown during parsing
 */
class ParseException extends Exception {
    public ParseException(String message) {
        super(message);
    }
}