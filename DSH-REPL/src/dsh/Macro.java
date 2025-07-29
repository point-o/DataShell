package dsh;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Macro {
    private final String name;
    private final List<String> rawLines = new ArrayList<>();
    private final List<List<Token>> tokenLines = new ArrayList<>();
    
    public Macro(String name) {
        this.name = name;
    }
    
    public void record(Scanner scanner, Tokenizer tokenizer) {
        System.out.println("╭─ Recording macro '" + name + "'");
        System.out.println("│ Commands: 'end' to finish, 'undo' to remove last line, 'show' to preview");
        System.out.println("╰─ Enter your macro lines:");
        
        while (true) {
            System.out.print(String.format("[%d] >>> ", rawLines.size() + 1));
            String line = scanner.nextLine().trim();
            
            if (line.equals("end")) {
                break;
            }
            
            if (line.equals("undo")) {
                handleUndo();
                continue;
            }
            
            if (line.equals("show")) {
                showCurrentMacro();
                continue;
            }
            
            if (line.isEmpty()) {
                System.out.println("│ Empty line ignored. Use 'end' to finish recording.");
                continue;
            }
            
            if (addLine(line, tokenizer)) {
                System.out.println("│ ✓ Line " + rawLines.size() + " added successfully");
            }
        }
        
        finalizeMacro();
    }
    
    private boolean addLine(String line, Tokenizer tokenizer) {
    	tokenizer.reset(line);
        Result<List<Token>> tokenResult = tokenizer.tokenize();
        
        if (tokenResult.isError()) {
            System.err.println("│ ✗ Invalid syntax - line not added");
            System.err.println("│   Error: " + tokenResult.getErrorMessage());
            System.err.println("│   Line: " + line);
            return false;
        }
        
        rawLines.add(line);
        tokenLines.add(tokenResult.getValue());
        return true;
    }
    
    private void handleUndo() {
        if (!rawLines.isEmpty()) {
            String removedLine = rawLines.remove(rawLines.size() - 1);
            tokenLines.remove(tokenLines.size() - 1);
            System.out.println("│ ↶ Removed: " + removedLine);
            System.out.println("│   Lines remaining: " + rawLines.size());
        } else {
            System.out.println("│ Nothing to undo - macro is empty");
        }
    }
    
    private void showCurrentMacro() {
        if (rawLines.isEmpty()) {
            System.out.println("│ Macro is currently empty");
            return;
        }
        
        System.out.println("│ Current macro contents:");
        for (int i = 0; i < rawLines.size(); i++) {
            System.out.printf("│ %2d: %s%n", i + 1, rawLines.get(i));
        }
        System.out.println("│ Total lines: " + rawLines.size());
    }
    
    private void finalizeMacro() {
        if (rawLines.isEmpty()) {
            System.out.println("╰─ ⚠ Macro '" + name + "' is empty - no lines recorded");
        } else {
            System.out.println("╰─ ✓ Macro '" + name + "' saved successfully with " + 
                             rawLines.size() + " line(s)");
        }
    }

    public void addRawLine(String line) {
        if (line != null && !line.trim().isEmpty()) {
            rawLines.add(line.trim());
        }
    }

    public boolean tokenizeAll(Tokenizer tokenizer) {
        tokenLines.clear();
        boolean allSuccessful = true;
        
        for (int i = 0; i < rawLines.size(); i++) {
            String line = rawLines.get(i);
            tokenizer.reset(line);
            Result<List<Token>> result = tokenizer.tokenize();
            
            if (result.isOk()) {
                tokenLines.add(result.getValue());
            } else {
                System.err.println("Warning: Invalid line " + (i + 1) + 
                                 " in macro '" + name + "': " + line);
                System.err.println("Error: " + result.getErrorMessage());
                allSuccessful = false;
                // Add empty token list to maintain line correspondence
                tokenLines.add(new ArrayList<>());
            }
        }
        
        return allSuccessful;
    }
    
    public List<List<Token>> getTokenLines() {
        return new ArrayList<>(tokenLines); // Return defensive copy
    }
    
    public List<String> getRawLines() {
        return new ArrayList<>(rawLines); // Return defensive copy
    }
    
    public String getName() {
        return name;
    }
    
    public int getLineCount() {
        return rawLines.size();
    }
    
    public boolean isEmpty() {
        return rawLines.isEmpty();
    }
    
    public void clear() {
        rawLines.clear();
        tokenLines.clear();
    }
}