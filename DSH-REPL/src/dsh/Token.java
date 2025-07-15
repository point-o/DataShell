package dsh;

import java.math.BigDecimal;

/**
* Represents a single token
*
*@author Ryan Pointer
*@version 7/14/25
*/
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final Value literal;
    private final int line;
    private final int column;

    public Token(TokenType type, String lexeme, Value literal, int line, int column) {
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

    public Value getLiteral() {
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

    public static Token number(String lexeme, ANumber value, int line, int column) {
        return new Token(TokenType.NUMBER, lexeme, value, line, column);
    }

    public static Token string(String lexeme, AString value, int line, int column) {
        return new Token(TokenType.STRING, lexeme, value, line, column);
    }

    public static Token bool(String lexeme, ABoolean value, int line, int column) {
        return new Token(TokenType.BOOLEAN, lexeme, value, line, column);
    }

    public static Token identifier(String lexeme, int line, int column) {
        return new Token(TokenType.IDENTIFIER, lexeme, line, column);
    }

    public static Token expression(String lexeme, int line, int column) {
        return new Token(TokenType.EXPRESSION, lexeme, line, column);
    }

    // assignment
    public static Token assign(String lexeme, int line, int column) {
        return new Token(TokenType.ASSIGN, lexeme, line, column);
    }

    // comparison
    public static Token equals(String lexeme, int line, int column) {
        return new Token(TokenType.EQUALS, lexeme, line, column);
    }

    public static Token notEquals(String lexeme, int line, int column) {
        return new Token(TokenType.NOT_EQUALS, lexeme, line, column);
    }

    public static Token lessThan(String lexeme, int line, int column) {
        return new Token(TokenType.LESS_THAN, lexeme, line, column);
    }

    public static Token greaterThan(String lexeme, int line, int column) {
        return new Token(TokenType.GREATER_THAN, lexeme, line, column);
    }

    public static Token lessEqual(String lexeme, int line, int column) {
        return new Token(TokenType.LESS_EQUAL, lexeme, line, column);
    }

    public static Token greaterEqual(String lexeme, int line, int column) {
        return new Token(TokenType.GREATER_EQUAL, lexeme, line, column);
    }

    // Logical operators
    public static Token and(String lexeme, int line, int column) {
        return new Token(TokenType.AND, lexeme, line, column);
    }

    public static Token or(String lexeme, int line, int column) {
        return new Token(TokenType.OR, lexeme, line, column);
    }

    public static Token not(String lexeme, int line, int column) {
        return new Token(TokenType.NOT, lexeme, line, column);
    }

    // Delimiters
    public static Token leftParen(String lexeme, int line, int column) {
        return new Token(TokenType.LPAREN, lexeme, line, column);
    }

    public static Token rightParen(String lexeme, int line, int column) {
        return new Token(TokenType.RPAREN, lexeme, line, column);
    }

    public static Token leftBracket(String lexeme, int line, int column) {
        return new Token(TokenType.LBRACKET, lexeme, line, column);
    }

    public static Token rightBracket(String lexeme, int line, int column) {
        return new Token(TokenType.RBRACKET, lexeme, line, column);
    }

    public static Token leftBrace(String lexeme, int line, int column) {
        return new Token(TokenType.LBRACE, lexeme, line, column);
    }

    public static Token rightBrace(String lexeme, int line, int column) {
        return new Token(TokenType.RBRACE, lexeme, line, column);
    }

    public static Token comma(String lexeme, int line, int column) {
        return new Token(TokenType.COMMA, lexeme, line, column);
    }

    public static Token semicolon(String lexeme, int line, int column) {
        return new Token(TokenType.SEMICOLON, lexeme, line, column);
    }

    // whitespace stuff
    public static Token newline(String lexeme, int line, int column) {
        return new Token(TokenType.NEWLINE, lexeme, line, column);
    }

    public static Token whitespace(String lexeme, int line, int column) {
        return new Token(TokenType.WHITESPACE, lexeme, line, column);
    }

    public static Token eof(int line, int column) {
        return new Token(TokenType.EOF, "", line, column);
    }

    // keywords
    public static Token ifKeyword(String lexeme, int line, int column) {
        return new Token(TokenType.IF, lexeme, line, column);
    }

    public static Token elseKeyword(String lexeme, int line, int column) {
        return new Token(TokenType.ELSE, lexeme, line, column);
    }

    public static Token whileKeyword(String lexeme, int line, int column) {
        return new Token(TokenType.WHILE, lexeme, line, column);
    }

    public static Token forKeyword(String lexeme, int line, int column) {
        return new Token(TokenType.FOR, lexeme, line, column);
    }

    public static Token trueKeyword(String lexeme, int line, int column) {
        return new Token(TokenType.TRUE, lexeme, line, column);
    }

    public static Token falseKeyword(String lexeme, int line, int column) {
        return new Token(TokenType.FALSE, lexeme, line, column);
    }

    public static Token nullKeyword(String lexeme, int line, int column) {
        return new Token(TokenType.NULL, lexeme, line, column);
    }

    // error
    public static Token invalid(String lexeme, int line, int column) {
        return new Token(TokenType.INVALID, lexeme, line, column);
    }
}