package dsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * Tokenizer optimized for REPL environments with Result-based error handling
 * 
 * usage loop:
 * reset(new input)
 * tokenize
 * 
 * @author Ryan Pointer
 * @version 7/20/25
 */
public class Tokenizer {
    private String input;
    private List<Token> tokens;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    
    private boolean needsMoreInput = false;
    
    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("null", TokenType.NULL);
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("not", TokenType.NOT);
    }
    
    public Tokenizer() {
        this.tokens = new ArrayList<>();
    }
    
    /**
     * Resets the tokenizer with new input while preserving line/column state
     */
    public void reset(String input) {
        this.input = input;
        this.current = 0;
        this.tokens.clear();
        this.needsMoreInput = false;
    }
    
    /**
     * Tokenizes the input, returning a Result containing either tokens or an error
     */
    public Result<List<Token>> tokenize() {
        while (!isAtEnd()) {
            scanToken();
            if (needsMoreInput) {
                return Result.error(Result.ErrorType.SYNTAX, "Incomplete input - needs continuation");
            }
        }
        
        tokens.add(Token.eof(line, column));
        return Result.ok(tokens);
    }
    
    private void scanToken() {
        int startColumn = column;
        char c = advance();
        
        switch (c) {
            case '(': addToken(Token.leftParen("(", line, startColumn)); break;
            case ')': addToken(Token.rightParen(")", line, startColumn)); break;
            case '[': addToken(Token.leftBracket("[", line, startColumn)); break;
            case ']': addToken(Token.rightBracket("]", line, startColumn)); break;
            case '{': addToken(Token.leftBrace("{", line, startColumn)); break;
            case '}': addToken(Token.rightBrace("}", line, startColumn)); break;
            case ',': addToken(Token.comma(",", line, startColumn)); break;
            
            case ':':
                if (isAlpha(peek())) {
                    command(startColumn);
                } else {
                    addToken(Token.colon(":", line, startColumn));
                }
                break;
                
            case ';':
                if (isAlpha(peek())) {
                    macro(startColumn);
                } else {
                    addToken(Token.semicolon(";", line, startColumn));
                }
                break;
                
            case '=':
                if (match('=')) {
                    addToken(Token.equals("==", line, startColumn));
                } else {
                    addToken(Token.assign("=", line, startColumn));
                }
                break;
            case '!':
                if (match('=')) {
                    addToken(Token.notEquals("!=", line, startColumn));
                } else {
                    addToken(Token.not("!", line, startColumn));
                }
                break;
            case '<':
                if (match('=')) {
                    addToken(Token.lessEqual("<=", line, startColumn));
                } else {
                    addToken(Token.lessThan("<", line, startColumn));
                }
                break;
            case '>':
                if (match('=')) {
                    addToken(Token.greaterEqual(">=", line, startColumn));
                } else {
                    addToken(Token.greaterThan(">", line, startColumn));
                }
                break;
                
            case ' ':
            case '\r':
            case '\t':
                addToken(Token.whitespace(String.valueOf(c), line, startColumn));
                break;
            case '\n':
                addToken(Token.newline("\n", line, startColumn));
                line++;
                column = 1;
                return;

            case '"':
                string(startColumn);
                break;
                
            case '#':
                expression(startColumn);
                break;
                
            default:
                if (isDigit(c)) {
                    number(startColumn);
                } else if (isAlpha(c)) {
                    identifier(startColumn);
                } else {
                    addToken(Token.invalid(String.valueOf(c), line, startColumn));
                }
                break;
        }
    }
    
    private void string(int startColumn) {
        StringBuilder value = new StringBuilder();
        
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 1;
            }
            char c = advance();
            
            if (c == '\\' && !isAtEnd()) {
                char escaped = advance();
                switch (escaped) {
                    case 'n': value.append('\n'); break;
                    case 't': value.append('\t'); break;
                    case 'r': value.append('\r'); break;
                    case '\\': value.append('\\'); break;
                    case '"': value.append('"'); break;
                    default: 
                        value.append('\\');
                        value.append(escaped);
                        break;
                }
            } else {
                value.append(c);
            }
        }
        
        if (isAtEnd()) {
            needsMoreInput = true;
            return;
        }
        
        advance(); // consume closing quote
        
        String lexeme = "\"" + value.toString() + "\"";
        addToken(Token.string(lexeme, new AString(value.toString()), line, startColumn));
    }
    
    private void command(int startColumn) {
        StringBuilder commandName = new StringBuilder();
        
        while (isAlphaNumeric(peek())) {
            commandName.append(advance());
        }
        
        String lexeme = ":" + commandName.toString();
        addToken(Token.command(lexeme, commandName.toString(), line, startColumn));
    }
    
    private void macro(int startColumn) {
        StringBuilder macroName = new StringBuilder();
        
        while (isAlphaNumeric(peek())) {
            macroName.append(advance());
        }
        
        String lexeme = ";" + macroName.toString();
        addToken(Token.macro(lexeme, macroName.toString(), line, startColumn));
    }
    
    private void expression(int startColumn) {
        StringBuilder exprContent = new StringBuilder();
        
        if (peek() == '{') {
            advance(); // consume {
            int braceCount = 1;
            
            while (braceCount > 0 && !isAtEnd()) {
                char c = advance();
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
                
                if (braceCount > 0) {
                    exprContent.append(c);
                }
            }
            
            if (braceCount > 0) {
                needsMoreInput = true;
                return;
            }
            
            String lexeme = "#{" + exprContent.toString() + "}";
            addToken(Token.expression(lexeme, line, startColumn));
        } else {
            while (isAlphaNumeric(peek())) {
                exprContent.append(advance());
            }
            
            String lexeme = "#" + exprContent.toString();
            addToken(Token.expression(lexeme, line, startColumn));
        }
    }
    
    private void number(int startColumn) {
        StringBuilder numStr = new StringBuilder();
        numStr.append(input.charAt(current - 1));
        
        while (isDigit(peek())) {
            numStr.append(advance());
        }
        
        if (peek() == '.' && isDigit(peekNext())) {
            numStr.append(advance());
            
            while (isDigit(peek())) {
                numStr.append(advance());
            }
        }
        
        String lexeme = numStr.toString();
        try {
            BigDecimal value = new BigDecimal(lexeme);
            addToken(Token.number(lexeme, new ANumber(value), line, startColumn));
        } catch (NumberFormatException e) {
            addToken(Token.invalid(lexeme, line, startColumn));
        }
    }
    
    private void identifier(int startColumn) {
        StringBuilder identifier = new StringBuilder();
        identifier.append(input.charAt(current - 1));
        
        while (isAlphaNumeric(peek())) {
            identifier.append(advance());
        }
        
        String lexeme = identifier.toString();
        TokenType type = keywords.get(lexeme);
        
        if (type != null) {
            switch (type) {
                case TRUE:
                    addToken(Token.bool(lexeme, new ABoolean(true), line, startColumn));
                    break;
                case FALSE:
                    addToken(Token.bool(lexeme, new ABoolean(false), line, startColumn));
                    break;
                case IF:
                    addToken(Token.ifKeyword(lexeme, line, startColumn));
                    break;
                case ELSE:
                    addToken(Token.elseKeyword(lexeme, line, startColumn));
                    break;
                case WHILE:
                    addToken(Token.whileKeyword(lexeme, line, startColumn));
                    break;
                case FOR:
                    addToken(Token.forKeyword(lexeme, line, startColumn));
                    break;
                case NULL:
                    addToken(Token.nullKeyword(lexeme, line, startColumn));
                    break;
                case AND:
                    addToken(Token.and(lexeme, line, startColumn));
                    break;
                case OR:
                    addToken(Token.or(lexeme, line, startColumn));
                    break;
                case NOT:
                    addToken(Token.not(lexeme, line, startColumn));
                    break;
                default:
                    addToken(Token.identifier(lexeme, line, startColumn));
            }
        } else {
            addToken(Token.identifier(lexeme, line, startColumn));
        }
    }
    
    // Helper methods
    private boolean isAtEnd() {
        return current >= input.length();
    }
    
    private char advance() {
        column++;
        return input.charAt(current++);
    }
    
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (input.charAt(current) != expected) return false;
        
        current++;
        column++;
        return true;
    }
    
    private char peek() {
        if (isAtEnd()) return '\0';
        return input.charAt(current);
    }
    
    private char peekNext() {
        if (current + 1 >= input.length()) return '\0';
        return input.charAt(current + 1);
    }
    
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }
    
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    
    private void addToken(Token token) {
        tokens.add(token);
    }
    
    /**
     * Returns true if the last scan indicated more input is needed
     */
    public boolean needsMoreInput() {
        return needsMoreInput;
    }
    
    /**
     * Convenience method to tokenize and filter out whitespace tokens
     */
    public Result<List<Token>> tokenizeNoWhitespace() {
        return tokenize().map(tokens -> {
            List<Token> filtered = new ArrayList<>();
            for (Token token : tokens) {
                if (token.getType() != TokenType.WHITESPACE && 
                    token.getType() != TokenType.NEWLINE) {
                    filtered.add(token);
                }
            }
            return filtered;
        });
    }
}