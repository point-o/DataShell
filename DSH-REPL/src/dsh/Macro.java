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

    public void record(Scanner scanner, ReplTokenizer tokenizer) {
        System.out.println("Recording macro '" + name + "'. Type 'end' to finish, 'undo' to remove last line.");
        while (true) {
            System.out.print(">>> ");
            String line = scanner.nextLine().trim();
            if (line.equals("end")) break;
            if (line.equals("undo")) {
                if (!rawLines.isEmpty()) {
                    rawLines.remove(rawLines.size() - 1);
                    tokenLines.remove(tokenLines.size() - 1);
                    System.out.println("Last line removed.");
                } else {
                    System.out.println("Nothing to undo.");
                }
                continue;
            }
            Result<List<Token>> tokenResult = tokenizer.tokenize(line);
            if (tokenResult.isError()) {
                System.err.println("Error: line invalid. Not added.");
                System.err.println("Details: " + tokenResult.getErrorMessage());
                continue;
            }
            rawLines.add(line);
            tokenLines.add(tokenResult.getValue());
            System.out.println("Line added.");
        }
        System.out.println("Macro '" + name + "' saved with " + tokenLines.size() + " line(s).");
    }

    public void addRawLine(String line) {
        rawLines.add(line);
    }

    public void tokenizeAll(ReplTokenizer tokenizer) {
        tokenLines.clear();
        for (String line : rawLines) {
            Result<List<Token>> result = tokenizer.tokenize(line);
            if (result.isOk()) {
                tokenLines.add(result.getValue());
            } else {
                System.err.println("Warning: Skipped invalid line in macro '" + name + "': " + line);
            }
        }
    }
    public List<List<Token>> getTokenLines() {
        return tokenLines;
    }
    public List<String> getRawLines() {
        return rawLines;
    }
    public String getName() {
        return name;
    }
}