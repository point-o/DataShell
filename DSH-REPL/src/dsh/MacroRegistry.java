package dsh;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
/**
 * Registry for user-defined macros. Stores macros in memory and persists them to disk.
 */
public class MacroRegistry {
    private final Map<String, Macro> macros = new HashMap<>();
    private final Path storageFile;
    public MacroRegistry(Path storageFile) {
        this.storageFile = storageFile;
        loadFromDisk().ifError(failure -> {
            System.err.println("Failed to load macros: " + failure.getErrorMessage());
        });
    }
    public Result<Void> add(Macro macro) {
        macros.put(macro.getName(), macro);
        return saveToDisk();
    }
    public Result<Void> delete(String name) {
        if (macros.remove(name) != null) {
            return saveToDisk();
        }
        return Result.error(Result.ErrorType.INVALID_ARGUMENT, "Macro not found: " + name);
    }
    public Optional<Macro> get(String name) {
        return Optional.ofNullable(macros.get(name));
    }
    public boolean contains(String name) {
        return macros.containsKey(name);
    }
    public Set<String> listNames() {
        return Collections.unmodifiableSet(macros.keySet());
    }
    private Result<Void> saveToDisk() {
        try (BufferedWriter writer = Files.newBufferedWriter(storageFile)) {
            for (Macro macro : macros.values()) {
                writer.write(";" + macro.getName());
                writer.newLine();
                for (String line : macro.getRawLines()) {
                    writer.write(line);
                    writer.newLine();
                }
                writer.write("end");
                writer.newLine();
            }
            return Result.ok(null);
        } catch (IOException e) {
            return Result.error(Result.ErrorType.RUNTIME, "Failed to save macros: " + e.getMessage(), e);
        }
    }
    private Result<Void> loadFromDisk() {
        if (!Files.exists(storageFile)) return Result.ok(null);
        try (BufferedReader reader = Files.newBufferedReader(storageFile)) {
            String line;
            Macro current = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(";")) {
                    current = new Macro(line.substring(1).trim());
                } else if (line.equals("end")) {
                    if (current != null) {
                        macros.put(current.getName(), current);
                        current = null;
                    }
                } else if (current != null) {
                    current.addRawLine(line);
                }
            }
            return Result.ok(null);
        } catch (IOException e) {
            return Result.error(Result.ErrorType.RUNTIME, "Failed to load macros: " + e.getMessage(), e);
        }
    }
}