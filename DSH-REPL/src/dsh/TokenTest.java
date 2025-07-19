package dsh;

import java.math.BigDecimal;
import java.util.List;

/**
 * Test class for the Tokenizer
 * 
 * @author Ryan Pointer
 * @version 7/19/25
 */
public class TokenTest {
    
    public static void main(String[] args) {
        System.out.println("Running Tokenizer Tests...\n");
        
        testBasicTokens();
        testNumbers();
        testStrings();
        testKeywords();
        testCommands();
        testMacros();
        testExpressions();
        testComparisons();
        testComplexExample();
        testEdgeCases();
        
        System.out.println("\nAll tests completed!");
    }
    
    private static void testBasicTokens() {
        System.out.println("=== Testing Basic Tokens ===");
        String input = "( ) [ ] { } , : ;";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenizeNoWhitespace();
        
        assertTokenType(tokens.get(0), TokenType.LPAREN, "(");
        assertTokenType(tokens.get(1), TokenType.RPAREN, ")");
        assertTokenType(tokens.get(2), TokenType.LBRACKET, "[");
        assertTokenType(tokens.get(3), TokenType.RBRACKET, "]");
        assertTokenType(tokens.get(4), TokenType.LBRACE, "{");
        assertTokenType(tokens.get(5), TokenType.RBRACE, "}");
        assertTokenType(tokens.get(6), TokenType.COMMA, ",");
        assertTokenType(tokens.get(7), TokenType.COLON, ":");
        assertTokenType(tokens.get(8), TokenType.SEMICOLON, ";");
        
        System.out.println("✓ Basic tokens test passed");
    }
    
    private static void testNumbers() {
        System.out.println("=== Testing Numbers ===");
        String input = "42 3.14159 0 123.456";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenizeNoWhitespace();
        
        assertTokenType(tokens.get(0), TokenType.NUMBER, "42");
        assertTokenType(tokens.get(1), TokenType.NUMBER, "3.14159");
        assertTokenType(tokens.get(2), TokenType.NUMBER, "0");
        assertTokenType(tokens.get(3), TokenType.NUMBER, "123.456");
        
        // Check that literals are correct
        ANumber num1 = (ANumber) tokens.get(0).getLiteral();
        ANumber num2 = (ANumber) tokens.get(1).getLiteral();
        assert num1.getValue().equals(new BigDecimal("42"));
        assert num2.getValue().equals(new BigDecimal("3.14159"));
        
        System.out.println("✓ Numbers test passed");
    }
    
    private static void testStrings() {
        System.out.println("=== Testing Strings ===");
        String input = "\"hello\" \"world with spaces\" \"escape\\ntest\" \"\"";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenizeNoWhitespace();
        
        assertTokenType(tokens.get(0), TokenType.STRING, "\"hello\"");
        assertTokenType(tokens.get(1), TokenType.STRING, "\"world with spaces\"");
        assertTokenType(tokens.get(2), TokenType.STRING, "\"escape\\ntest\"");
        assertTokenType(tokens.get(3), TokenType.STRING, "\"\"");
        
        // Check string values
        AString str1 = (AString) tokens.get(0).getLiteral();
        AString str2 = (AString) tokens.get(1).getLiteral();
        assert str1.getValue().equals("hello");
        assert str2.getValue().equals("world with spaces");
        
        System.out.println("✓ Strings test passed");
    }
    
    private static void testKeywords() {
        System.out.println("=== Testing Keywords ===");
        String input = "if else while for true false null and or not";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenizeNoWhitespace();
        
        assertTokenType(tokens.get(0), TokenType.IF, "if");
        assertTokenType(tokens.get(1), TokenType.ELSE, "else");
        assertTokenType(tokens.get(2), TokenType.WHILE, "while");
        assertTokenType(tokens.get(3), TokenType.FOR, "for");
        assertTokenType(tokens.get(4), TokenType.BOOLEAN, "true");
        assertTokenType(tokens.get(5), TokenType.BOOLEAN, "false");
        assertTokenType(tokens.get(6), TokenType.NULL, "null");
        assertTokenType(tokens.get(7), TokenType.AND, "and");
        assertTokenType(tokens.get(8), TokenType.OR, "or");
        assertTokenType(tokens.get(9), TokenType.NOT, "not");
        
        // Check boolean values
        ABoolean bool1 = (ABoolean) tokens.get(4).getLiteral();
        ABoolean bool2 = (ABoolean) tokens.get(5).getLiteral();
        assert bool1.getValue() == true;
        assert bool2.getValue() == false;
        
        System.out.println("✓ Keywords test passed");
    }
    
    private static void testCommands() {
        System.out.println("=== Testing Commands ===");
        String input = ":echo :ls :help123 : standalone";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenizeNoWhitespace();
        
        assertTokenType(tokens.get(0), TokenType.COMMAND, ":echo");
        assertTokenType(tokens.get(1), TokenType.COMMAND, ":ls");
        assertTokenType(tokens.get(2), TokenType.COMMAND, ":help123");
        assertTokenType(tokens.get(3), TokenType.COLON, ":");
        assertTokenType(tokens.get(4), TokenType.IDENTIFIER, "standalone");
        
        // Check command names
        AString cmd1 = (AString) tokens.get(0).getLiteral();
        AString cmd2 = (AString) tokens.get(1).getLiteral();
        assert cmd1.getValue().equals("echo");
        assert cmd2.getValue().equals("ls");
        
        System.out.println("✓ Commands test passed");
    }
    
    private static void testMacros() {
        System.out.println("=== Testing Macros ===");
        String input = ";define ;include ;myMacro ; standalone";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenizeNoWhitespace();
        
        assertTokenType(tokens.get(0), TokenType.MACRO, ";define");
        assertTokenType(tokens.get(1), TokenType.MACRO, ";include");
        assertTokenType(tokens.get(2), TokenType.MACRO, ";myMacro");
        assertTokenType(tokens.get(3), TokenType.SEMICOLON, ";");
        assertTokenType(tokens.get(4), TokenType.IDENTIFIER, "standalone");
        
        // Check macro names
        AString macro1 = (AString) tokens.get(0).getLiteral();
        AString macro2 = (AString) tokens.get(1).getLiteral();
        assert macro1.getValue().equals("define");
        assert macro2.getValue().equals("include");
        
        System.out.println("✓ Macros test passed");
    }
    
    private static void testExpressions() {
        System.out.println("=== Testing Expressions ===");
        String input = "#var #{x + y} #{nested{braces}} #simple";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenizeNoWhitespace();
        
        assertTokenType(tokens.get(0), TokenType.EXPRESSION, "#var");
        assertTokenType(tokens.get(1), TokenType.EXPRESSION, "#{x + y}");
        assertTokenType(tokens.get(2), TokenType.EXPRESSION, "#{nested{braces}}");
        assertTokenType(tokens.get(3), TokenType.EXPRESSION, "#simple");
        
        System.out.println("✓ Expressions test passed");
    }
    
    private static void testComparisons() {
        System.out.println("=== Testing Comparisons ===");
        String input = "== != < > <= >= = !";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenizeNoWhitespace();
        
        assertTokenType(tokens.get(0), TokenType.EQUALS, "==");
        assertTokenType(tokens.get(1), TokenType.NOT_EQUALS, "!=");
        assertTokenType(tokens.get(2), TokenType.LESS_THAN, "<");
        assertTokenType(tokens.get(3), TokenType.GREATER_THAN, ">");
        assertTokenType(tokens.get(4), TokenType.LESS_EQUAL, "<=");
        assertTokenType(tokens.get(5), TokenType.GREATER_EQUAL, ">=");
        assertTokenType(tokens.get(6), TokenType.ASSIGN, "=");
        assertTokenType(tokens.get(7), TokenType.NOT, "!");
        
        System.out.println("✓ Comparisons test passed");
    }
    
    private static void testComplexExample() {
        System.out.println("=== Testing Complex Example ===");
        String input = "name = \"John Doe\"; age = 25; if (age >= 18) { :echo #{name} }; ;greet";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenizeNoWhitespace();
        
        // Just verify we get the expected number of meaningful tokens
        assert tokens.size() > 15; // Should have many tokens
        
        // Check a few key tokens
        assertTokenType(tokens.get(0), TokenType.IDENTIFIER, "name");
        assertTokenType(tokens.get(1), TokenType.ASSIGN, "=");
        assertTokenType(tokens.get(2), TokenType.STRING, "\"John Doe\"");
        
        // Find the command token
        boolean foundCommand = false;
        boolean foundExpression = false;
        boolean foundMacro = false;
        
        for (Token token : tokens) {
            if (token.getType() == TokenType.COMMAND && token.getLexeme().equals(":echo")) {
                foundCommand = true;
            }
            if (token.getType() == TokenType.EXPRESSION && token.getLexeme().equals("#{name}")) {
                foundExpression = true;
            }
            if (token.getType() == TokenType.MACRO && token.getLexeme().equals(";greet")) {
                foundMacro = true;
            }
        }
        
        assert foundCommand : "Should find :echo command";
        assert foundExpression : "Should find #{name} expression";
        assert foundMacro : "Should find ;greet macro";
        
        System.out.println("✓ Complex example test passed");
    }
    
    private static void testEdgeCases() {
        System.out.println("=== Testing Edge Cases ===");
        
        // Empty string
        Tokenizer emptyTokenizer = new Tokenizer("");
        List<Token> emptyTokens = emptyTokenizer.tokenize();
        assertTokenType(emptyTokens.get(0), TokenType.EOF, "");
        
        // Just whitespace
        Tokenizer wsTokenizer = new Tokenizer("   \n  \t  ");
        List<Token> wsTokens = wsTokenizer.tokenizeNoWhitespace();
        assertTokenType(wsTokens.get(0), TokenType.EOF, "");
        
        // Invalid characters
        Tokenizer invalidTokenizer = new Tokenizer("@#$%^");
        List<Token> invalidTokens = invalidTokenizer.tokenizeNoWhitespace();
        // Should handle # as expression, others as invalid or identifiers
        
        // Unterminated string
        Tokenizer unterminatedTokenizer = new Tokenizer("\"unterminated");
        List<Token> unterminatedTokens = unterminatedTokenizer.tokenizeNoWhitespace();
        assertTokenType(unterminatedTokens.get(0), TokenType.INVALID, "Unterminated string");
        
        System.out.println("✓ Edge cases test passed");
    }
    
    // Helper method to assert token type and lexeme
    private static void assertTokenType(Token token, TokenType expectedType, String expectedLexeme) {
        assert token.getType() == expectedType : 
            "Expected " + expectedType + " but got " + token.getType() + " for lexeme: " + token.getLexeme();
        assert token.getLexeme().equals(expectedLexeme) : 
            "Expected lexeme '" + expectedLexeme + "' but got '" + token.getLexeme() + "'";
    }
    
    // Helper method to print tokens for debugging
    private static void printTokens(List<Token> tokens) {
        System.out.println("Tokens:");
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.printf("  [%d] %s\n", i, token);
        }
    }
}