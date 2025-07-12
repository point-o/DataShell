package dsh;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Expression parsing calculator with Result-based error handling
 * 
 * @author Ryan Pointer
 * @version 7/12/25
 */
public class Calculator {
    private final Environment env;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*");

    public Calculator(Environment env) {
        this.env = env;
    }

    public Result<Value> evaluate(String expression) {
        try {
            expression = expression.trim();
            if (expression.isEmpty()) {
                return Result.error(Result.ErrorType.VALIDATION, "Empty expression");
            }
            
            if (!hasBalancedParentheses(expression)) {
                return Result.error(Result.ErrorType.SYNTAX, "Unbalanced parentheses in expression");
            }
            
            expression = removeOuterParentheses(expression);
            
            // Try to parse as number
            try {
                BigDecimal number = new BigDecimal(expression);
                return Result.ok(new ANumber(number));
            } catch (NumberFormatException e) {
                // Not a number, continue parsing
            }
            
            // Try to parse as array literal
            if (expression.startsWith("[") && expression.endsWith("]")) {
                return parseArrayLiteral(expression);
            }
            
            // Try to parse as string literal
            if (expression.startsWith("\"") && expression.endsWith("\"") && expression.length() >= 2) {
                return Result.ok(new AString(expression.substring(1, expression.length() - 1)));
            }

            // Try to parse as boolean literals
            if ("true".equals(expression)) {
                return Result.ok(new ABoolean(true));
            }
            if ("false".equals(expression)) {
                return Result.ok(new ABoolean(false));
            }
            if ("null".equals(expression)) {
                return Result.ok(new ANull());
            }

            // Try to parse as variable
            if (isValidVariableName(expression)) {
                if (env.has(expression)) {
                    return Result.ok(env.get(expression));
                }
                return Result.error(Result.ErrorType.RUNTIME, "Undefined variable '" + expression + "'");
            }

            // Try to parse as binary operation
            String[] parts = splitExpressionByPrecedence(expression);
            if (parts != null) {
                return evaluate(parts[0])
                    .flatMap(left -> evaluate(parts[2])
                        .flatMap(right -> performOperation(left, right, parts[1])));
            }

            return Result.error(Result.ErrorType.SYNTAX, "Invalid expression '" + expression + "'");
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, "Unexpected error during evaluation: " + e.getMessage(), e);
        }
    }

    private String removeOuterParentheses(String expression) {
        while (expression.startsWith("(") && expression.endsWith(")") && 
               hasBalancedParentheses(expression.substring(1, expression.length() - 1))) {
            expression = expression.substring(1, expression.length() - 1);
        }
        return expression;
    }

    private Result<Value> performOperation(Value left, Value right, String operator) {
        try {
            switch (operator) {
                case "+": return left.add(right);
                case "-": return left.subtract(right);
                case "*": return left.multiply(right);
                case "/": return left.divide(right);
                case "%": return left.modulo(right);
                case "^": return left.power(right);
                default: return Result.error(Result.ErrorType.SYNTAX, "Unknown operator '" + operator + "'");
            }
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, "Operation failed: " + e.getMessage(), e);
        }
    }

    private boolean hasBalancedParentheses(String expr) {
        int parenBalance = 0;
        int bracketBalance = 0;
        boolean inString = false;
        char prev = 0;
        
        for (char c : expr.toCharArray()) {
            if (c == '"' && prev != '\\') {
                inString = !inString;
            } else if (!inString) {
                if (c == '(') parenBalance++;
                if (c == ')') parenBalance--;
                if (c == '[') bracketBalance++;
                if (c == ']') bracketBalance--;
                if (parenBalance < 0 || bracketBalance < 0) return false;
            }
            prev = c;
        }
        return parenBalance == 0 && bracketBalance == 0;
    }

    private boolean isValidVariableName(String name) {
        return VARIABLE_PATTERN.matcher(name).matches();
    }

    private String[] splitExpressionByPrecedence(String expression) {
        String[][] precedenceLevels = {
            {"+", "-"},
            {"*", "/", "%"},
            {"^"}
        };
        
        for (String[] operators : precedenceLevels) {
            for (String op : operators) {
                int index = findOperatorIndex(expression, op);
                if (index > 0) {
                    return new String[]{
                        expression.substring(0, index).trim(),
                        op,
                        expression.substring(index + 1).trim()
                    };
                }
            }
        }
        
        return null;
    }

    private int findOperatorIndex(String expression, String operator) {
        int parenLevel = 0;
        int bracketLevel = 0;
        boolean inString = false;
        char prev = 0;
        int lastIndex = -1;
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (c == '"' && prev != '\\') {
                inString = !inString;
            } else if (!inString) {
                if (c == '(') {
                    parenLevel++;
                } else if (c == ')') {
                    parenLevel--;
                } else if (c == '[') {
                    bracketLevel++;
                } else if (c == ']') {
                    bracketLevel--;
                } else if (parenLevel == 0 && bracketLevel == 0 && operator.equals(String.valueOf(c))) {
                    if (i > 0 && i < expression.length() - 1) {
                        if (operator.equals("-") && isPartOfNumber(expression, i)) {
                            continue;
                        }
                        lastIndex = i;
                    }
                }
            }
            prev = c;
        }
        
        return lastIndex;
    }

    private boolean isPartOfNumber(String expression, int minusIndex) {
        if (minusIndex == 0) return true;
        
        char prevChar = expression.charAt(minusIndex - 1);
        return prevChar == '(' || prevChar == '+' || prevChar == '-' || 
               prevChar == '*' || prevChar == '/' || prevChar == '%' || prevChar == '^';
    }

    private Result<Value> parseArrayLiteral(String expression) {
        try {
            String content = expression.substring(1, expression.length() - 1).trim();
            if (content.isEmpty()) {
                return Result.ok(new AList<>(new java.util.ArrayList<>()));
            }
            
            String[] elements = content.split(",");
            java.util.ArrayList<Value> values = new java.util.ArrayList<>();
            
            for (int i = 0; i < elements.length; i++) {
                Result<Value> elementResult = evaluate(elements[i].trim());
                if (elementResult.isError()) {
                    return Result.error(Result.ErrorType.SYNTAX, 
                        "Invalid array element at index " + i + ": " + elementResult.getErrorMessage(),
                        elementResult.getCause());
                }
                values.add(elementResult.getValue());
            }
            
            return Result.ok(new AList<>(values));
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, "Failed to parse array literal: " + e.getMessage(), e);
        }
    }
}