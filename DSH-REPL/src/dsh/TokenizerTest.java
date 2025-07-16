package dsh;

import java.util.List;

/**
 * Test class for the DSH Tokenizer
 * 
 * @author Ryan Pointer
 * @version 7/14/25
 */
public class TokenizerTest {
    
    public static void main(String[] args) {
        System.out.println("=== DSH Tokenizer Test Suite ===\n");
        
        // Test 1: Basic assignment and arithmetic
        testCase("Basic Assignment", "x = 5 + 3");
        
        // Test 2: String literals
        testCase("String Literals", "name = \"John Doe\"\ngreeting = 'Hello World'");
        
        // Test 3: Boolean and comparison operators
        testCase("Boolean & Comparison", "result = x > 5 && y <= 10\nflag = true != false");
        
        // Test 4: Control structures
        testCase("Control Structures", "if (x == 5) {\n    print(\"found\")\n} else {\n    print(\"not found\")\n}");
        
        // Test 5: Loops
        testCase("Loops", "for (i = 0; i < 10; i++) {\n    while (running) {\n        process()\n    }\n}");
        
        // Test 6: Calculator expressions (arithmetic only)
        testCase("Calculator Expressions", "x + y * z\n2.5 ^ 3 - 1\na / b % c");
        
        // Test 7: All operators (non-arithmetic)
        testCase("All Operators", "a = b == c != d < e > f <= g >= h && i || !j");
        
        // Test 8: Arrays and objects
        testCase("Arrays & Objects", "arr = [1, 2, 3]\nobj = {key: value, num: 42}");
        
        // Test 9: Null and keywords
        testCase("Keywords", "if (value == null) return false");
        
        // Test 10: Error cases
        testCase("Error Cases", "unterminated = \"string\nwrong = &single\nbad = |pipe");
        
        // Test 11: Numbers
        testCase("Numbers", "int_num = 42\nfloat_num = 3.14159\nneg_num = -25");
        
        // Test 12: Mixed content
        testCase("Mixed Content", 
            "function calculate(x, y) {\n" +
            "    if (x > 0 && y != null) {\n" +
            "        return x * 2.5 + y\n" +
            "    }\n" +
            "    return 0\n" +
            "}");
    }
    
    private static void testCase(String testName, String input) {
        System.out.println("--- " + testName + " ---");
        System.out.println("Input: " + input);
        System.out.println("Tokens:");
        
        try {
            Tokenizer tokenizer = new Tokenizer(input);
            List<Token> tokens = tokenizer.tokenize();
            
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                System.out.printf("%2d: %-15s %s (line %d, col %d)%n", 
                    i, token.getType(), token.toString(), token.getLine(), token.getColumn());
            }
            
            System.out.println("Total tokens: " + tokens.size());
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
}