package dsh;

/**
 * A class for defining tokens, simplified to 5 types.
 * 
 * @author Ryan Pointer
 * @version 7/29/25
 */
public class Token {
    public enum TokenType {
        EXPRESSION,    // #
        COMMAND,       // :
        MACRO,         // ;
        ASSIGNMENT,    // =
        VARIABLE,      // identifiers like x, myVar, _test
        LITERAL        // "1", 1, "hello", true, etc.
    }
    
    private final TokenType type;
    private final String value;
    private final int startPos;
    private final int endPos;
    private final Value literalValue; // null for non-literals
    
    // Constructor for non-literal tokens
    public Token(TokenType type, String value, int startPos, int endPos) {
        this.type = type;
        this.value = value;
        this.startPos = startPos;
        this.endPos = endPos;
        this.literalValue = null;
    }
    
    // Constructor for literal tokens with parsed Value
    public Token(TokenType type, String value, int startPos, int endPos, Value literalValue) {
        this.type = type;
        this.value = value;
        this.startPos = startPos;
        this.endPos = endPos;
        this.literalValue = literalValue;
    }
    
    public TokenType getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }
    
    public int getStartPos() {
        return startPos;
    }
    
    public int getEndPos() {
        return endPos;
    }
    
    public int getLength() {
        return endPos - startPos;
    }
    
    public Value getLiteralValue() {
        return literalValue;
    }
    
    public boolean hasLiteralValue() {
        return literalValue != null;
    }
    
    public boolean isExpression() {
        return type == TokenType.EXPRESSION;
    }
    
    public boolean isCommand() {
        return type == TokenType.COMMAND;
    }
    
    public boolean isMacro() {
        return type == TokenType.MACRO;
    }
    
    public boolean isAssignment() {
        return type == TokenType.ASSIGNMENT;
    }
    
    public boolean isVariable() {
        return type == TokenType.VARIABLE;
    }
    
    public boolean isLiteral() {
        return type == TokenType.LITERAL;
    }
    
    // Helper method to determine if a literal is numeric
    public boolean isNumericLiteral() {
        if (!isLiteral()) return false;
        return literalValue != null && literalValue.type().equals("number");
    }
    
    // Helper method to determine if a literal is a string (quoted)
    public boolean isStringLiteral() {
        if (!isLiteral()) return false;
        return literalValue != null && literalValue.type().equals("string");
    }
    
    // Helper method to determine if a literal is a boolean
    public boolean isBooleanLiteral() {
        if (!isLiteral()) return false;
        return literalValue != null && literalValue.type().equals("boolean");
    }
    
    // Get the actual string content without quotes (legacy method)
    public String getStringContent() {
        if (isStringLiteral() && literalValue != null) {
            return literalValue.toString();
        }
        // Fallback for tokens without parsed Value
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
    
    // Get numeric value (legacy method)
    public double getNumericValue() {
        if (isNumericLiteral() && literalValue != null) {
            return ((java.math.BigDecimal) literalValue.getValue()).doubleValue();
        }
        // Fallback parsing
        if (isLiteral()) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Token is not a numeric literal");
            }
        }
        throw new IllegalStateException("Token is not a numeric literal");
    }
    
    @Override
    public String toString() {
        if (literalValue != null) {
            return String.format("Token{type=%s, value='%s', pos=%d-%d, literalValue=%s}", 
                               type, value, startPos, endPos, literalValue);
        }
        return String.format("Token{type=%s, value='%s', pos=%d-%d}", 
                           type, value, startPos, endPos);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Token token = (Token) obj;
        return startPos == token.startPos &&
               endPos == token.endPos &&
               type == token.type &&
               value.equals(token.value) &&
               (literalValue == null ? token.literalValue == null : literalValue.equals(token.literalValue));
    }
    
    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + startPos;
        result = 31 * result + endPos;
        result = 31 * result + (literalValue != null ? literalValue.hashCode() : 0);
        return result;
    }
}