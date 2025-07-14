package dsh;

/**
 * Represents the different types of tokens
 * 
 * @author Ryan Pointer
 * @version 7/14/25
 */
public enum TokenType {
    NUMBER,
    STRING,
    BOOLEAN,

    IDENTIFIER,    // variable names, command names
    
    PLUS,          // +
    MINUS,         // -
    MULTIPLY,      // *
    DIVIDE,        // /
    MODULO,        // %
    POWER,         // ^ or **
    
    ASSIGN,        // =
    
    LPAREN,        // (
    RPAREN,        // )
    LBRACKET,      // [
    RBRACKET,      // ]
    LBRACE,        // {
    RBRACE,        // }
    COMMA,         // ,
    SEMICOLON,     // ;
    
    NEWLINE,       // \n (might need this?)
    WHITESPACE,    // spaces, tabs
    EOF,           // end of file
    
    IF, ELSE, WHILE, FOR, TRUE, FALSE, NULL,
    
    INVALID        // for malformed tokens (how do we handle this?)
}