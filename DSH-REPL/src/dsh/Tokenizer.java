package dsh;

import java.util.*;

/**
 * Tokenizer
 * 
 * @author Ryan Pointer
 * @version 7/14/25
 */
public class Tokenizer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    
    private static final Map<String, TokenType> KEYWORDS = Map.of(
        "if", TokenType.IF,
        "else", TokenType.ELSE,
        "while", TokenType.WHILE,
        "for", TokenType.FOR,
        "true", TokenType.TRUE,
        "false", TokenType.FALSE,
        "null", TokenType.NULL
    );
    
    public Tokenizer(String source) {
        this.source = source;
    }
    
    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        
        tokens.add(Token.eof(line, column));
        return tokens;
    }
    
    private void scanToken() {
        char c = advance();
        
        switch (c) {
            case ' ':
            case '\r':
            case '\t':
                column++;
                break;
            case '\n':
                tokens.add(Token.newline("\n", line, column));
                line++;
                column = 1;
                break;
            case '(':
                addToken(Token.leftParen("(", line, column - 1));
                break;
            case ')':
                addToken(Token.rightParen(")", line, column - 1));
                break;
            case '[':
                addToken(Token.leftBracket("[", line, column - 1));
                break;
            case ']':
                addToken(Token.rightBracket("]", line, column - 1));
                break;
            case '{':
                addToken(Token.leftBrace("{", line, column - 1));
                break;
            case '}':
                addToken(Token.rightBrace("}", line, column - 1));
                break;
            case ',':
                addToken(Token.comma(",", line, column - 1));
                break;
            case ';':
                addToken(Token.semicolon(";", line, column - 1));
                break;
            case '!':
                if (match('=')) {
                    addToken(Token.notEquals("!=", line, column - 2));
                } else {
                    addToken(Token.not("!", line, column - 1));
                }
                break;
            case '=':
                if (match('=')) {
                    addToken(Token.equals("==", line, column - 2));
                } else {
                    addToken(Token.assign("=", line, column - 1));
                }
                break;
            case '<':
                if (match('=')) {
                    addToken(Token.lessEqual("<=", line, column - 2));
                } else {
                    addToken(Token.lessThan("<", line, column - 1));
                }
                break;
            case '>':
                if (match('=')) {
                    addToken(Token.greaterEqual(">=", line, column - 2));
                } else {
                    addToken(Token.greaterThan(">", line, column - 1));
                }
                break;
            case '&':
                if (match('&')) {
                    addToken(Token.and("&&", line, column - 2));
                } else {
                    addToken(Token.invalid("&", line, column - 1));
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(Token.or("||", line, column - 2));
                } else {
                    addToken(Token.invalid("|", line, column - 1));
                }
                break;
            case '"':
                string();
                break;
            case '\'':
                singleQuoteString();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else if (isExpressionStart(c)) {
                    expression();
                } else {
                    addToken(Token.invalid(String.valueOf(c), line, column - 1));
                }
                break;
        }
    }
    
    private void string() {
        int startLine = line;
        int startColumn = column - 1;
        
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            advance();
        }
        
        if (isAtEnd()) {
            addToken(Token.invalid("Unterminated string", startLine, startColumn));
            return;
        }
        
        advance(); // closing "
        
        String value = source.substring(start + 1, current - 1);
        // Assuming AString constructor exists
        addToken(Token.string(source.substring(start, current), 
                             new AString(value), startLine, startColumn));
    }
    
    private void singleQuoteString() {
        int startLine = line;
        int startColumn = column - 1;
        
        while (peek() != '\'' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            advance();
        }
        
        if (isAtEnd()) {
            addToken(Token.invalid("Unterminated string", startLine, startColumn));
            return;
        }
        
        advance(); // closing '
        
        String value = source.substring(start + 1, current - 1);
        addToken(Token.string(source.substring(start, current), 
                             new AString(value), startLine, startColumn));
    }
    
    private void number() {
        int startColumn = column - 1;
        
        while (isDigit(peek())) {
            advance();
        }
        
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); 
            while (isDigit(peek())) {
                advance();
            }
        }
        
        String lexeme = source.substring(start, current);
        try {
            addToken(Token.number(lexeme, new ANumber(Double.parseDouble(lexeme)), 
                                 line, startColumn));
        } catch (NumberFormatException e) {
            addToken(Token.invalid(lexeme, line, startColumn));
        }
    }
    
    private void identifier() {
        int startColumn = column - 1;
        
        while (isAlphaNumeric(peek())) {
            advance();
        }
        
        String text = source.substring(start, current);
        TokenType type = KEYWORDS.get(text);
        
        if (type != null) {
            switch (type) {
                case IF:
                    addToken(Token.ifKeyword(text, line, startColumn));
                    break;
                case ELSE:
                    addToken(Token.elseKeyword(text, line, startColumn));
                    break;
                case WHILE:
                    addToken(Token.whileKeyword(text, line, startColumn));
                    break;
                case FOR:
                    addToken(Token.forKeyword(text, line, startColumn));
                    break;
                case TRUE:
                    addToken(Token.bool(text, new ABoolean(true), line, startColumn));
                    break;
                case FALSE:
                    addToken(Token.bool(text, new ABoolean(false), line, startColumn));
                    break;
                case NULL:
                    addToken(Token.nullKeyword(text, line, startColumn));
                    break;
			default:
				break;
            }
        } else {
            addToken(Token.identifier(text, line, startColumn));
        }
    }
    
    private void expression() {
        int startColumn = column - 1;
        int depth = 0;
        
        while (!isAtEnd()) {
            char c = peek();
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth < 0) break; 
            } else if (depth == 0 && (Character.isWhitespace(c) || c == ';' || c == '\n')) {
                break; // End of expression at top level
            }
            advance();
        }
        
        String expr = source.substring(start, current);
        addToken(Token.expression(expr, line, startColumn));
    }
    
    private boolean isExpressionStart(char c) {
    	// i WILL be changing this logic
        return c == '(' || c == '+' || c == '-' || c == '*' || c == '/' || c == '%';
    }
    
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        
        current++;
        column++;
        return true;
    }
    
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    
    private char advance() {
        column++;
        return source.charAt(current++);
    }
    
    private void addToken(Token token) {
        tokens.add(token);
    }
    
    private boolean isAtEnd() {
        return current >= source.length();
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
}