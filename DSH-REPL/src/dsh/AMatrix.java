package dsh;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to represent a 2D Matrix of Value objects with enforced homogeneity of Value types.
 * 
 * @author Ryan Pointer
 * @version 7/12/25
 */
public class AMatrix implements Value {
    private List<List<Value>> matrix;
    private int rows;
    private int cols;
    private Class<? extends Value> elementType; // Enforced type of elements in matrix
    
    public AMatrix() {
        this.matrix = new ArrayList<>();
        this.rows = 0;
        this.cols = 0;
        this.elementType = null; // No type yet
    }

    public AMatrix(List<List<Value>> matrix) {
        this();
        for (List<Value> row : matrix) {
            if (!row.isEmpty()) {
                checkAndSetElementType(row.get(0));
            }
            for (Value val : row) {
                checkAndSetElementType(val);
            }
            this.matrix.add(new ArrayList<>(row));
        }
        this.rows = this.matrix.size();
        this.cols = this.matrix.isEmpty() ? 0 : this.matrix.get(0).size();
        normalizeMatrix();
    }
    
    public AMatrix(String csvFilePath, boolean hasHeader) throws IOException {
        this();
        loadFromCSV(csvFilePath, hasHeader);
    }
    
    public AMatrix(String csvFilePath) throws IOException {
        this(csvFilePath, false);
    }
    
    private void checkAndSetElementType(Value val) {
        if (val == null || val instanceof ANull) return; // allow nulls, why not.
        if (this.elementType == null) {
            this.elementType = val.getClass();
        } else if (!this.elementType.equals(val.getClass())) {
            throw new IllegalArgumentException("All matrix elements must be of the same type. Found: "
                + val.getClass().getSimpleName() + ", expected: " + this.elementType.getSimpleName());
        }
    }
    
    /**
     * Load data from CSV file
     */
    private void loadFromCSV(String csvFilePath, boolean hasHeader) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (hasHeader && firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] values = parseCSVLine(line);
                List<Value> row = new ArrayList<>();
                for (String value : values) {
                    Value parsedValue = parseValue(value.trim());
                    checkAndSetElementType(parsedValue);
                    row.add(parsedValue);
                }
                this.matrix.add(row);
                firstLine = false;
            }
        }
        this.rows = this.matrix.size();
        this.cols = this.matrix.isEmpty() ? 0 : this.matrix.get(0).size();
        normalizeMatrix();
    }
    
    private String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    private Value parseValue(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        try {
            BigDecimal number = new BigDecimal(value);
            return new ANumber(number);
        } catch (NumberFormatException e) {
            // Not a number, continue
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return new ABoolean(Boolean.parseBoolean(value));
        }
        if (value.isEmpty() || value.equalsIgnoreCase("null")) {
            return new ANull();
        }
        return new AString(value);
    }

    private void normalizeMatrix() {
        if (this.matrix.isEmpty()) return;
        int maxCols = 0;
        for (List<Value> row : this.matrix) {
            maxCols = Math.max(maxCols, row.size());
        }
        for (List<Value> row : this.matrix) {
            while (row.size() < maxCols) {
                // Add default null element of enforced type or ANull
                if (this.elementType == null || this.elementType.equals(ANull.class)) {
                    row.add(new ANull());
                } else {
                    try {
                        // Try to create a default instance via no-arg constructor if exists
                        Value defaultValue = this.elementType.getDeclaredConstructor().newInstance();
                        row.add(defaultValue);
                    } catch (Exception e) {
                        // fallback to ANull if instantiation fails
                        row.add(new ANull());
                    }
                }
            }
        }
        this.cols = maxCols;
    }

    public Value get(int row, int col) {
        if (row >= 0 && row < this.rows && col >= 0 && col < this.cols) {
            return this.matrix.get(row).get(col);
        }
        return new ANull();
    }
    
    public Value set(int row, int col, Value value) {
        if (value != null) {
            checkAndSetElementType(value);
        }
        if (row >= 0 && row < this.rows && col >= 0 && col < this.cols) {
            return this.matrix.get(row).set(col, value);
        }
        return value;
    }
    
    public AList<Value> getRow(int row) {
        if (row >= 0 && row < this.rows) {
            return new AList<>(this.matrix.get(row));
        }
        return new AList<>();
    }
    
    public AList<Value> getColumn(int col) {
        if (col >= 0 && col < this.cols) {
            List<Value> column = new ArrayList<>();
            for (List<Value> row : this.matrix) {
                column.add(row.get(col));
            }
            return new AList<>(column);
        }
        return new AList<>();
    }
    
    public void addRow(List<Value> row) {
        if (row.isEmpty()) return; // nothing to add
        
        for (Value val : row) {
            checkAndSetElementType(val);
        }
        
        List<Value> newRow = new ArrayList<>(row);
        while (newRow.size() < this.cols) {
            if (this.elementType == null || this.elementType.equals(ANull.class)) {
                newRow.add(new ANull());
            } else {
                try {
                    Value defaultValue = this.elementType.getDeclaredConstructor().newInstance();
                    newRow.add(defaultValue);
                } catch (Exception e) {
                    newRow.add(new ANull());
                }
            }
        }
        this.matrix.add(newRow);
        this.rows++;
        
        if (newRow.size() > this.cols) {
            this.cols = newRow.size();
            normalizeMatrix();
        }
    }

    public int getRows() {
        return this.rows;
    }
    
    public int getCols() {
        return this.cols;
    }
    
    public boolean isEmpty() {
        return this.rows == 0 || this.cols == 0;
    }
    
    public void clear() {
        this.matrix.clear();
        this.rows = 0;
        this.cols = 0;
        this.elementType = null;
    }
    
    @Override
    public Result<Value> asNumber() {
        return Result.ok(new ANumber(new BigDecimal(this.rows * this.cols)));
    }
    
    @Override
    public Result<Value> asString() {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < this.rows; i++) {
                for (int j = 0; j < this.cols; j++) {
                    sb.append(this.matrix.get(i).get(j).toString());
                    if (j < this.cols - 1) sb.append(", ");
                }
                if (i < this.rows - 1) sb.append("\n");
            }
            return Result.ok(new AString(sb.toString()));
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, "Error converting matrix to string: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Result<Value> asBoolean() {
        return Result.ok(new ABoolean(!isEmpty()));
    }
    
    @Override
    public String type() {
        return "matrix";
    }
    
    @Override
    public String toString() {
        Result<Value> stringResult = asString();
        if (stringResult.isError()) {
            return "[Error: " + stringResult.getErrorMessage() + "]";
        }
        return stringResult.getValue().toString();
    }
    
    @Override
    public Object getValue() {
        return this.matrix;
    }
}
