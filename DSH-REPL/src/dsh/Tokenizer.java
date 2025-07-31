package dsh;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tokenizer.
 * Handles expressions (#), commands (:), macros (;), literals, and variables.
 * 
 * @author Ryan Pointer
 * @version 7/29/25
 */
public class Tokenizer {
    private String input;
    private int position;
    private final List<Token> tokens;
    
    private static final Pattern NUMBER_PATTERN = 
        Pattern.compile("-?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?");
    private static final Pattern STRING_PATTERN = 
        Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
    private static final Pattern BOOLEAN_PATTERN = 
        Pattern.compile("true|false");
    private static final Pattern FILEPATH_PATTERN = 
        Pattern.compile("[a-zA-Z0-9_][a-zA-Z0-9_./-]*\\.[a-zA-Z0-9]+");
    private static final Pattern IDENTIFIER_PATTERN = 
        Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    private static final Pattern SPECIAL_CHARS = 
        Pattern.compile("[#:;=]");
    
    // Reserved keywords that can't be used as identifiers (in case i add control flow)
    private static final List<String> RESERVED_KEYWORDS = 
        List.of("if", "else", "while", "for", "return", "break", "continue");
    
    public Tokenizer(String input) {
        this.input = input != null ? input : "";
        this.position = 0;
        this.tokens = new ArrayList<>();
    }
    
    public void reset(String newInput) {
        this.input = newInput != null ? newInput : "";
        this.position = 0;
        this.tokens.clear();
    }
    
    public Result<List<Token>> tokenize() {
        while (position < input.length()) {
            skipWhitespace();
            if (position >= input.length()) break;
            
            char currentChar = input.charAt(position);
            
            // Handle special prefix tokens
            switch (currentChar) {
                case '#':
                    if (!tokenizeExpression().isOk()) return getLastError();
                    continue;
                case ':':
                    if (!tokenizeCommand().isOk()) return getLastError();
                    continue;
                case ';':
                    if (!tokenizeMacro().isOk()) return getLastError();
                    continue;
                case '=':
                    tokens.add(new Token(Token.TokenType.ASSIGNMENT, "=", position, position + 1));
                    position++;
                    continue;
            }
            
            // Try to match literals
            Result<Boolean> literalResult = tryTokenizeLiteral();
            if (literalResult.isError()) return getLastError();
            if (literalResult.getValue()) continue;
            
            // Unrecognized character
            return Result.error(Result.ErrorType.SYNTAX, 
                String.format("Unexpected character '%c' at position %d", 
                    currentChar, position));
        }
        
        return Result.ok(new ArrayList<>(tokens));
    }
    
    private void skipWhitespace() {
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            position++;
        }
    }
    
    private Result<Void> tokenizeExpression() {
        return tokenizePrefixedToken(Token.TokenType.EXPRESSION, '#');
    }
    
    private Result<Void> tokenizeCommand() {
        return tokenizePrefixedToken(Token.TokenType.COMMAND, ':');
    }
    
    private Result<Void> tokenizeMacro() {
        return tokenizePrefixedToken(Token.TokenType.MACRO, ';');
    }
    
    private Result<Void> tokenizePrefixedToken(Token.TokenType type, char prefix) {
        int startPos = position;
        position++; // Skip prefix
        
        if (type == Token.TokenType.EXPRESSION) {
            return tokenizeNestedExpression(startPos);
        }
        
        int endPos = findTokenEnd(type);
        if (endPos == startPos + 1) {
            return Result.error(Result.ErrorType.SYNTAX,
                String.format("Empty %s at position %d", 
                    type.name().toLowerCase(), startPos));
        }
        
        String content = input.substring(startPos, endPos);
        tokens.add(new Token(type, content, startPos, endPos));
        position = endPos;
        
        return Result.ok(null);
    }
    
    private Result<Void> tokenizeNestedExpression(int startPos) {
        int currentPos = position;
        boolean inString = false;
        
        // First check if it's a parenthesized expression
        if (currentPos < input.length() && input.charAt(currentPos) == '(') {
            int parenLevel = 1;
            currentPos++;
            
            while (currentPos < input.length() && parenLevel > 0) {
                char c = input.charAt(currentPos);
                
                if (c == '"' && (currentPos == 0 || input.charAt(currentPos - 1) != '\\')) {
                    inString = !inString;
                }
                
                if (!inString) {
                    if (c == '(') {
                        parenLevel++;
                    } else if (c == ')') {
                        parenLevel--;
                    }
                }
                
                currentPos++;
            }
            
            if (parenLevel > 0) {
                return Result.error(Result.ErrorType.SYNTAX,
                    String.format("Unterminated parenthesized expression starting at position %d", startPos));
            }
        } else {
            // Regular expression - go until whitespace or special character
            while (currentPos < input.length()) {
                char c = input.charAt(currentPos);
                if (Character.isWhitespace(c) || SPECIAL_CHARS.matcher(String.valueOf(c)).matches()) {
                    break;
                }
                currentPos++;
            }
        }
        
        if (currentPos == startPos + 1) {
            return Result.error(Result.ErrorType.SYNTAX,
                String.format("Empty expression at position %d", startPos));
        }
        
        String content = input.substring(startPos, currentPos);
        tokens.add(new Token(Token.TokenType.EXPRESSION, content, startPos, currentPos));
        position = currentPos;
        
        return Result.ok(null);
    }
    
    private int findTokenEnd(Token.TokenType type) {
        int pos = position;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (type == Token.TokenType.EXPRESSION) {
                // For expressions, we stop at whitespace or special chars
                if (Character.isWhitespace(c) || SPECIAL_CHARS.matcher(String.valueOf(c)).matches()) {
                    break;
                }
            } else {
                // For commands and macros, we only stop at whitespace
                if (Character.isWhitespace(c)) {
                    break;
                }
            }
            pos++;
        }
        return pos;
    }
    
    private Result<Boolean> tryTokenizeLiteral() {
        String remaining = input.substring(position);
        
        // Try string first (has clear delimiters)
        Matcher stringMatcher = STRING_PATTERN.matcher(remaining);
        if (stringMatcher.lookingAt()) {
            return tokenizeString(stringMatcher.group());
        }
        
        // Try number
        Matcher numberMatcher = NUMBER_PATTERN.matcher(remaining);
        if (numberMatcher.lookingAt()) {
            return tokenizeNumber(numberMatcher.group());
        }
        
        // Try boolean
        Matcher booleanMatcher = BOOLEAN_PATTERN.matcher(remaining);
        if (booleanMatcher.lookingAt()) {
            int endPos = position + booleanMatcher.group().length();
            if (endPos < input.length() && Character.isLetterOrDigit(input.charAt(endPos))) {
                return tryTokenizeIdentifier();
            }
            return tokenizeBoolean(booleanMatcher.group());
        }
        
        // Try filepath (before identifier to catch file.ext patterns)
        Matcher filepathMatcher = FILEPATH_PATTERN.matcher(remaining);
        if (filepathMatcher.lookingAt()) {
            return tokenizeFilepath(filepathMatcher.group());
        }
        
        // Try identifier
        return tryTokenizeIdentifier();
    }
    
    private Result<Boolean> tokenizeString(String matched) {
        int startPos = position;
        int endPos = position + matched.length();
        
        try {
            String content = parseStringLiteral(matched);
            Value stringValue = new AString(content);
            
            tokens.add(new Token(Token.TokenType.LITERAL, matched, startPos, endPos, stringValue));
            position = endPos;
            return Result.ok(true);
        } catch (Exception e) {
            return Result.error(Result.ErrorType.SYNTAX, 
                String.format("Invalid string literal at position %d: %s",
                    startPos, e.getMessage()));
        }
    }
    
    private Result<Boolean> tokenizeNumber(String matched) {
        int startPos = position;
        int endPos = position + matched.length();
        
        try {
            BigDecimal numberValue = new BigDecimal(matched);
            if (matched.startsWith(".")) {
                return Result.error(Result.ErrorType.SYNTAX,
                    String.format("Invalid number format at position %d: leading decimal point",
                        startPos));
            }
            
            Value numValue = new ANumber(numberValue);
            tokens.add(new Token(Token.TokenType.LITERAL, matched, startPos, endPos, numValue));
            position = endPos;
            return Result.ok(true);
        } catch (NumberFormatException e) {
            return Result.error(Result.ErrorType.SYNTAX,
                String.format("Invalid number format at position %d: %s",
                    startPos, matched));
        }
    }
    
    private Result<Boolean> tokenizeBoolean(String matched) {
        int startPos = position;
        int endPos = position + matched.length();
        
        boolean boolValue = Boolean.parseBoolean(matched);
        Value booleanValue = new ABoolean(boolValue);
        
        tokens.add(new Token(Token.TokenType.LITERAL, matched, startPos, endPos, booleanValue));
        position = endPos;
        return Result.ok(true);
    }
    
    private Result<Boolean> tokenizeFilepath(String matched) {
        int startPos = position;
        int endPos = position + matched.length();
        
        // Create a string value for the filepath
        Value filepathValue = new AString(matched);
        tokens.add(new Token(Token.TokenType.LITERAL, matched, startPos, endPos, filepathValue));
        position = endPos;
        return Result.ok(true);
    }
    
    private Result<Boolean> tryTokenizeIdentifier() {
        String remaining = input.substring(position);
        Matcher identifierMatcher = IDENTIFIER_PATTERN.matcher(remaining);
        
        if (identifierMatcher.lookingAt()) {
            String matched = identifierMatcher.group();
            int startPos = position;
            int endPos = position + matched.length();
            
            if (RESERVED_KEYWORDS.contains(matched)) {
                return Result.error(Result.ErrorType.SYNTAX,
                    String.format("Reserved keyword '%s' used as identifier at position %d",
                        matched, startPos));
            }
            
            tokens.add(new Token(Token.TokenType.VARIABLE, matched, startPos, endPos));
            position = endPos;
            return Result.ok(true);
        }
        
        return Result.ok(false);
    }
    
    private String parseStringLiteral(String quotedString) {
        String content = quotedString.substring(1, quotedString.length() - 1);
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\\' && i + 1 < content.length()) {
                char next = content.charAt(i + 1);
                switch (next) {
                    case 'n': result.append('\n'); break;
                    case 't': result.append('\t'); break;
                    case 'r': result.append('\r'); break;
                    case '\\': result.append('\\'); break;
                    case '"': result.append('"'); break;
                    default: 
                        throw new IllegalArgumentException(
                            String.format("Invalid escape sequence '\\%c'", next));
                }
                i++; // Skip the next character
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    private Result<List<Token>> getLastError() {
        Token lastToken = tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
        String context = getContextAroundPosition(position, 20);
        return Result.error(Result.ErrorType.SYNTAX,
            String.format("Tokenization error near '%s' at position %d. Context: %s",
                lastToken != null ? lastToken.getValue() : "", position, context));
    }
    
    public String getContextAroundPosition(int pos, int contextLength) {
        int start = Math.max(0, pos - contextLength);
        int end = Math.min(input.length(), pos + contextLength);
        return input.substring(start, end).replace("\n", "\\n");
    }
}