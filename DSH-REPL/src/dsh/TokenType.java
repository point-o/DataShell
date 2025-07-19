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

    IDENTIFIER, // variable names, command names

    EXPRESSION, // to be handled independently by the calculator class

    ASSIGN, // =

    EQUALS,      // ==
    NOT_EQUALS,  // !=
    LESS_THAN,   // <
    GREATER_THAN, // >
    LESS_EQUAL,  // <=
    GREATER_EQUAL, // >=

    AND,         // &&
    OR,          // ||
    NOT,         // !

    LPAREN,      // (
    RPAREN,      // )
    LBRACKET,    // [
    RBRACKET,    // ]
    LBRACE,      // {
    RBRACE,      // }
    COMMA,       // ,
    COLON,       // : (command prefix)
    SEMICOLON,   // ; (macro prefix)

    NEWLINE,     // \n (might need this? i dunno)
    WHITESPACE,  // spaces, tabs
    EOF,         // end of file

    IF, ELSE, WHILE, FOR, TRUE, FALSE, NULL,
    
    COMMAND, MACRO,

    INVALID      // for malformed tokens (how do we handle this?)
}