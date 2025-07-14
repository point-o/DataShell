package dsh;

import java.math.BigDecimal;

/**
 * Represents a single token
 * 
 * @author Ryan Pointer
 * @version 7/14/25
 */
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final Object literal;
    private final int line;
    private final int column;
    
    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }
    
    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, null, line, column);
    }
    
    public TokenType getType() {
        return type;
    }
    
    public String getLexeme() {
        return lexeme;
    }
    
    public Object getLiteral() {
        return literal;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
    
    public String toString() {
        if (literal != null) {
            return type + "(" + literal + ")";
        }
        return type + "('" + lexeme + "')";
    }
    
    public static Token number(String lexeme, BigDecimal value, int line, int column) {
        return new Token(TokenType.NUMBER, lexeme, value, line, column);
    }
    
    public static Token string(String lexeme, String value, int line, int column) {
        return new Token(TokenType.STRING, lexeme, value, line, column);
    }
    
    public static Token bool(String lexeme, Boolean value, int line, int column) {
        return new Token(TokenType.BOOLEAN, lexeme, value, line, column);
    }
    
    public static Token identifier(String lexeme, int line, int column) {
        return new Token(TokenType.IDENTIFIER, lexeme, line, column);
    }
    
    public static Token eof(int line, int column) {
        return new Token(TokenType.EOF, "", line, column);
    }
    
    public static Token invalid(String lexeme, int line, int column) {
        return new Token(TokenType.INVALID, lexeme, line, column);
    }
}