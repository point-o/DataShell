package dsh;

import java.util.List;

public class TokenizerTest {
    
    public static void main(String[] args) {
        testTokenizer("#1+2+3", "Simple expression");
        testTokenizer("x = 42", "Variable assignment");
        testTokenizer("myVar = \"hello\"", "Variable with string");
        testTokenizer("_test = true", "Underscore variable");
        testTokenizer(":print hello", "Command with parameter");
        testTokenizer(";macro", "Macro");
        testTokenizer("\"hello world\"", "String literal");
        testTokenizer("true false", "Boolean literals");
        testTokenizer("#x+y x = \"test\" :cmd var ;mac", "Mixed tokens with variables");
        testTokenizer("", "Empty input");
        testTokenizer("   #1+2   ", "Whitespace handling");
        testTokenizer("123var", "Number followed by identifier");
        testTokenizer("trueVar", "Boolean-like identifier");
        testTokenizer("@invalid", "Invalid character");
        testTokenizer("\"unterminated", "Unterminated string");
    }
    
    private static void testTokenizer(String input, String description) {
        System.out.println("\n=== " + description + " ===");
        System.out.println("Input: \"" + input + "\"");
        
        Tokenizer tokenizer = new Tokenizer(input);
        Result<List<Token>> result = tokenizer.tokenize();
        
        if (result.isOk()) {
            List<Token> tokens = result.getValue();
            System.out.println("Success! Found " + tokens.size() + " tokens:");
            
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                System.out.printf("  [%d] %s\n", i, formatToken(token));
            }
        } else {
            System.out.println("ERROR: " + result.getErrorMessage());
            System.out.println("Type: " + result.getErrorType());
        }
    }
    
    private static String formatToken(Token token) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-12s", token.getType()));
        sb.append(String.format(" '%s'", token.getValue()));
        sb.append(String.format(" [%d-%d]", token.getStartPos(), token.getEndPos()));
        
        if (token.hasLiteralValue()) {
            Value literal = token.getLiteralValue();
            sb.append(String.format(" -> %s(%s)", literal.type(), literal.getValue()));
        }
        
        return sb.toString();
    }
}