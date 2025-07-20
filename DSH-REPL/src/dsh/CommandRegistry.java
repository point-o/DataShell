package dsh;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * A class to hold commands
 *
 * @author Ryan Pointer
 * @version 7/16/25
 */
public class CommandRegistry {
    private Map<String, Command> commands;

    public CommandRegistry() {
        this.commands = new HashMap<>();
        initializeCommands();
    }

    private void initializeCommands() {
        registerCommand("load", new Command(
            "Load CSV file as matrix",
            "load <filename> [hasheader]",
            this::loadMatrix,
            2
        ));
        
        registerCommand("peek", new Command(
            "Show first few rows of matrix",
            "peek <matrix> [rows=5]",
            this::peekMatrix,
            2
        ));
        
        registerCommand("size", new Command(
            "Show matrix dimensions",
            "size <matrix>",
            this::matrixSize,
            1
        ));
        
        registerCommand("cols", new Command(
            "Show column summary",
            "cols <matrix>",
            this::columnSummary,
            1
        ));
        
        registerCommand("sum", new Command(
            "Sum of numeric values in matrix/row/column",
            "sum <matrix> [row|col] [index]",
            this::sumValues,
            3
        ));
        
        registerCommand("count", new Command(
            "Count non-null values",
            "count <matrix> [row|col] [index]",
            this::countValues,
            3
        ));
        
        registerCommand("unique", new Command(
            "Count unique values in column",
            "unique <matrix> <col_index>",
            this::uniqueValues,
            2
        ));
        
        registerCommand("find", new Command(
            "Find rows where column equals value",
            "find <matrix> <col_index> <value>",
            this::findRows,
            3
        ));

        registerCommand("row", new Command(
            "Extract row as list",
            "row <matrix> <index>",
            this::getRow,
            2
        ));
        
        registerCommand("col", new Command(
            "Extract column as list", 
            "col <matrix> <index>",
            this::getColumn,
            2
        ));
        
        registerCommand("range", new Command(
            "Get numeric range (min-max) of data",
            "range <matrix|list> [col_index]",
            this::getRange,
            2
        ));
        
        registerCommand("sample", new Command(
            "Get random sample of rows",
            "sample <matrix> [count=5]",
            this::sampleRows,
            2
        ));
        
        registerCommand("sort", new Command(
            "Sort matrix by column or sort list",
            "sort <matrix|list> [col_index]",
            this::sortData,
            2
        ));
        
        registerCommand("filter", new Command(
            "Filter data by condition",
            "filter <matrix|list> <col_index|all> <op> <value>",
            this::filterData,
            4
        ));
        
        registerCommand("head", new Command(
            "Show first N items",
            "head <matrix|list> [count=10]",
            this::headData,
            2
        ));
        
        registerCommand("tail", new Command(
            "Show last N items", 
            "tail <matrix|list> [count=10]",
            this::tailData,
            2
        ));
        
        registerCommand("len", new Command(
            "Get length/size",
            "len <matrix|list>",
            this::getLength,
            1
        ));
        
        registerCommand("slice", new Command(
            "Get subset of data",
            "slice <matrix|list> <start> [end]",
            this::sliceData,
            3
        ));
        
        registerCommand("contains", new Command(
            "Check if value exists",
            "contains <matrix|list> <value> [col_index]", 
            this::containsValue,
            3
        ));
        
        registerCommand("stats", new Command(
            "Quick statistics summary",
            "stats <matrix|list> [col_index]",
            this::getStats,
            2
        ));
    }
    
    private Value loadMatrix(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide filename");
        }
        
        Result<Value> fileResult = args[0].asString();
        if (fileResult.isError()) {
            return new AString("Error: Invalid filename");
        }
        
        String filename = fileResult.getValue().toString();
        boolean hasHeader = false;
        
        if (args.length >= 2) {
            Result<Value> headerResult = args[1].asBoolean();
            if (!headerResult.isError()) {
                hasHeader = ((ABoolean) headerResult.getValue()).getValue();
            }
        }
        
        try {
            AMatrix matrix = new AMatrix(filename, hasHeader);
            return new AString("Loaded " + matrix.getRows() + "x" + matrix.getCols() + " matrix");
        } catch (IOException e) {
            return new AString("Error: " + e.getMessage());
        }
    }
    
    private Value peekMatrix(Environment context, Value... args) {
        if (args.length == 0 || !(args[0] instanceof AMatrix)) {
            return new AString("Error: Provide matrix");
        }
        
        AMatrix matrix = (AMatrix) args[0];
        int rows = 5; // default
        
        if (args.length >= 2) {
            Result<Value> rowsResult = args[1].asNumber();
            if (!rowsResult.isError()) {
                rows = ((ANumber) rowsResult.getValue()).getValue().intValue();
            }
        }
        
        if (matrix.isEmpty()) {
            return new AString("Matrix is empty");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("First ").append(Math.min(rows, matrix.getRows())).append(" rows:\n");
        
        for (int i = 0; i < Math.min(rows, matrix.getRows()); i++) {
            for (int j = 0; j < matrix.getCols(); j++) {
                sb.append(matrix.get(i, j).toString());
                if (j < matrix.getCols() - 1) sb.append("\t");
            }
            sb.append("\n");
        }
        
        return new AString(sb.toString());
    }
    
    private Value getRow(Environment context, Value... args) {
        if (args.length != 2 || !(args[0] instanceof AMatrix)) {
            return new AString("Error: row <matrix> <index>");
        }
        
        AMatrix matrix = (AMatrix) args[0];
        Result<Value> indexResult = args[1].asNumber();
        if (indexResult.isError()) {
            return new AString("Error: Invalid row index");
        }
        
        int index = ((ANumber) indexResult.getValue()).getValue().intValue();
        return matrix.getRow(index);
    }
    
    private Value getColumn(Environment context, Value... args) {
        if (args.length != 2 || !(args[0] instanceof AMatrix)) {
            return new AString("Error: col <matrix> <index>");
        }
        
        AMatrix matrix = (AMatrix) args[0];
        Result<Value> indexResult = args[1].asNumber();
        if (indexResult.isError()) {
            return new AString("Error: Invalid column index");
        }
        
        int index = ((ANumber) indexResult.getValue()).getValue().intValue();
        return matrix.getColumn(index);
    }
    
    private Value getRange(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide matrix or list");
        }
        
        BigDecimal min = null;
        BigDecimal max = null;
        
        if (args[0] instanceof AMatrix) {
            AMatrix matrix = (AMatrix) args[0];
            if (args.length >= 2) {
                // Specific column
                Result<Value> colResult = args[1].asNumber();
                if (colResult.isError()) {
                    return new AString("Error: Invalid column index");
                }
                int col = ((ANumber) colResult.getValue()).getValue().intValue();
                
                for (int i = 0; i < matrix.getRows(); i++) {
                    Value val = matrix.get(i, col);
                    Result<Value> numResult = val.asNumber();
                    if (!numResult.isError()) {
                        BigDecimal num = ((ANumber) numResult.getValue()).getValue();
                        if (min == null || num.compareTo(min) < 0) min = num;
                        if (max == null || num.compareTo(max) > 0) max = num;
                    }
                }
            } else {
                for (int i = 0; i < matrix.getRows(); i++) {
                    for (int j = 0; j < matrix.getCols(); j++) {
                        Value val = matrix.get(i, j);
                        Result<Value> numResult = val.asNumber();
                        if (!numResult.isError()) {
                            BigDecimal num = ((ANumber) numResult.getValue()).getValue();
                            if (min == null || num.compareTo(min) < 0) min = num;
                            if (max == null || num.compareTo(max) > 0) max = num;
                        }
                    }
                }
            }
        } else if (args[0] instanceof AList) {
            AList<?> list = (AList<?>) args[0];
            for (int i = 0; i < list.size(); i++) {
                Value val = list.get(i);
                Result<Value> numResult = val.asNumber();
                if (!numResult.isError()) {
                    BigDecimal num = ((ANumber) numResult.getValue()).getValue();
                    if (min == null || num.compareTo(min) < 0) min = num;
                    if (max == null || num.compareTo(max) > 0) max = num;
                }
            }
        } else {
            return new AString("Error: First argument must be matrix or list");
        }
        
        if (min == null) {
            return new AString("No numeric values found");
        }
        
        return new AString("Range: " + min + " to " + max + " (span: " + max.subtract(min) + ")");
    }
    
    private Value sampleRows(Environment context, Value... args) {
        if (args.length == 0 || !(args[0] instanceof AMatrix)) {
            return new AString("Error: sample <matrix> [count]");
        }
        
        AMatrix matrix = (AMatrix) args[0];
        int count = 5;
        
        if (args.length >= 2) {
            Result<Value> countResult = args[1].asNumber();
            if (!countResult.isError()) {
                count = ((ANumber) countResult.getValue()).getValue().intValue();
            }
        }
        
        if (matrix.isEmpty()) {
            return new AString("Matrix is empty");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Random sample of ").append(Math.min(count, matrix.getRows())).append(" rows:\n");
        
        java.util.Random rand = new java.util.Random();
        java.util.Set<Integer> selected = new java.util.HashSet<>();
        
        while (selected.size() < Math.min(count, matrix.getRows())) {
            selected.add(rand.nextInt(matrix.getRows()));
        }
        
        for (int row : selected) {
            sb.append("[").append(row).append("] ");
            for (int j = 0; j < Math.min(5, matrix.getCols()); j++) {
                sb.append(matrix.get(row, j).toString());
                if (j < Math.min(5, matrix.getCols()) - 1) sb.append("\t");
            }
            if (matrix.getCols() > 5) sb.append("\t...");
            sb.append("\n");
        }
        
        return new AString(sb.toString());
    }
    
    private Value sortData(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide data to sort");
        }
        
        if (args[0] instanceof AMatrix) {
            AMatrix matrix = (AMatrix) args[0];
            if (args.length < 2) {
                return new AString("Error: Specify column to sort by");
            }
            
            Result<Value> colResult = args[1].asNumber();
            if (colResult.isError()) {
                return new AString("Error: Invalid column index");
            }
            
            int col = ((ANumber) colResult.getValue()).getValue().intValue();
            
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < matrix.getRows(); i++) {
                indices.add(i);
            }
            
            indices.sort((a, b) -> {
                Value valA = matrix.get(a, col);
                Value valB = matrix.get(b, col);
                return valA.toString().compareTo(valB.toString());
            });
            
            StringBuilder sb = new StringBuilder();
            sb.append("Sorted by column ").append(col).append(" (showing first 10):\n");
            for (int i = 0; i < Math.min(10, indices.size()); i++) {
                int row = indices.get(i);
                sb.append("[").append(row).append("] ");
                for (int j = 0; j < Math.min(5, matrix.getCols()); j++) {
                    sb.append(matrix.get(row, j).toString());
                    if (j < Math.min(5, matrix.getCols()) - 1) sb.append("\t");
                }
                sb.append("\n");
            }
            
            return new AString(sb.toString());
        }
        
        return new AString("Sort not implemented for this type");
    }
    
    private Value filterData(Environment context, Value... args) {
        if (args.length < 4) {
            return new AString("Error: filter <data> <col|all> <op> <value>");
        }
        
        String op = args[2].toString();
        String filterValue = args[3].toString();
        
        if (args[0] instanceof AMatrix) {
            AMatrix matrix = (AMatrix) args[0];
            String colSpec = args[1].toString();
            
            List<Integer> matches = new ArrayList<>();
            
            if ("all".equals(colSpec)) {
                for (int i = 0; i < matrix.getRows(); i++) {
                    for (int j = 0; j < matrix.getCols(); j++) {
                        Value val = matrix.get(i, j);
                        if (matchesCondition(val, op, filterValue)) {
                            matches.add(i);
                            break; 
                        }
                    }
                }
            } else {
                Result<Value> colResult = args[1].asNumber();
                if (colResult.isError()) {
                    return new AString("Error: Invalid column index");
                }
                int col = ((ANumber) colResult.getValue()).getValue().intValue();
                
                for (int i = 0; i < matrix.getRows(); i++) {
                    Value val = matrix.get(i, col);
                    if (matchesCondition(val, op, filterValue)) {
                        matches.add(i);
                    }
                }
            }
            
            if (matches.isEmpty()) {
                return new AString("No matches found");
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Found ").append(matches.size()).append(" matches:\n");
            for (int i = 0; i < Math.min(10, matches.size()); i++) {
                int row = matches.get(i);
                sb.append("[").append(row).append("] ");
                for (int j = 0; j < Math.min(5, matrix.getCols()); j++) {
                    sb.append(matrix.get(row, j).toString());
                    if (j < Math.min(5, matrix.getCols()) - 1) sb.append("\t");
                }
                sb.append("\n");
            }
            if (matches.size() > 10) {
                sb.append("... (").append(matches.size() - 10).append(" more)");
            }
            
            return new AString(sb.toString());
        }
        
        return new AString("Filter not implemented for this type");
    }
    
    private boolean matchesCondition(Value val, String op, String filterValue) {
        String valStr = val.toString();
        
        switch (op) {
            case "=":
            case "==":
                return valStr.equals(filterValue);
            case "!=":
                return !valStr.equals(filterValue);
            case "contains":
                return valStr.contains(filterValue);
            case "startswith":
                return valStr.startsWith(filterValue);
            case "endswith":
                return valStr.endsWith(filterValue);
            case ">":
                try {
                    Result<Value> numResult = val.asNumber();
                    if (!numResult.isError()) {
                        BigDecimal num = ((ANumber) numResult.getValue()).getValue();
                        return num.compareTo(new BigDecimal(filterValue)) > 0;
                    }
                } catch (NumberFormatException e) {}
                return false;
            case "<":
                try {
                    Result<Value> numResult = val.asNumber();
                    if (!numResult.isError()) {
                        BigDecimal num = ((ANumber) numResult.getValue()).getValue();
                        return num.compareTo(new BigDecimal(filterValue)) < 0;
                    }
                } catch (NumberFormatException e) {}
                return false;
            default:
                return valStr.equals(filterValue);
        }
    }
    
    private Value headData(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide data");
        }
        
        int count = 10;
        if (args.length >= 2) {
            Result<Value> countResult = args[1].asNumber();
            if (!countResult.isError()) {
                count = ((ANumber) countResult.getValue()).getValue().intValue();
            }
        }
        
        if (args[0] instanceof AMatrix) {
            return peekMatrix(context, args[0], new ANumber(BigDecimal.valueOf(count)));
        } else if (args[0] instanceof AList) {
            AList<?> list = (AList<?>) args[0];
            StringBuilder sb = new StringBuilder();
            sb.append("First ").append(Math.min(count, list.size())).append(" items:\n");
            
            for (int i = 0; i < Math.min(count, list.size()); i++) {
                sb.append("[").append(i).append("] ").append(list.get(i).toString()).append("\n");
            }
            
            return new AString(sb.toString());
        }
        
        return new AString("Error: Unsupported data type");
    }
    
    private Value tailData(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide data");
        }
        
        int count = 10;
        if (args.length >= 2) {
            Result<Value> countResult = args[1].asNumber();
            if (!countResult.isError()) {
                count = ((ANumber) countResult.getValue()).getValue().intValue();
            }
        }
        
        if (args[0] instanceof AMatrix) {
            AMatrix matrix = (AMatrix) args[0];
            StringBuilder sb = new StringBuilder();
            int start = Math.max(0, matrix.getRows() - count);
            sb.append("Last ").append(matrix.getRows() - start).append(" rows:\n");
            
            for (int i = start; i < matrix.getRows(); i++) {
                sb.append("[").append(i).append("] ");
                for (int j = 0; j < Math.min(5, matrix.getCols()); j++) {
                    sb.append(matrix.get(i, j).toString());
                    if (j < Math.min(5, matrix.getCols()) - 1) sb.append("\t");
                }
                sb.append("\n");
            }
            
            return new AString(sb.toString());
        } else if (args[0] instanceof AList) {
            AList<?> list = (AList<?>) args[0];
            StringBuilder sb = new StringBuilder();
            int start = Math.max(0, list.size() - count);
            sb.append("Last ").append(list.size() - start).append(" items:\n");
            
            for (int i = start; i < list.size(); i++) {
                sb.append("[").append(i).append("] ").append(list.get(i).toString()).append("\n");
            }
            
            return new AString(sb.toString());
        }
        
        return new AString("Error: Unsupported data type");
    }
    
    private Value getLength(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide data");
        }
        
        if (args[0] instanceof AMatrix) {
            AMatrix matrix = (AMatrix) args[0];
            return new AString(matrix.getRows() + " rows");
        } else if (args[0] instanceof AList) {
            AList<?> list = (AList<?>) args[0];
            return new AString(list.size() + " items");
        }
        
        return new AString("Error: Unsupported data type");
    }
    
    private Value sliceData(Environment context, Value... args) {
        if (args.length < 2) {
            return new AString("Error: slice <data> <start> [end]");
        }
        
        Result<Value> startResult = args[1].asNumber();
        if (startResult.isError()) {
            return new AString("Error: Invalid start index");
        }
        int start = ((ANumber) startResult.getValue()).getValue().intValue();
        
        int end = -1;
        if (args.length >= 3) {
            Result<Value> endResult = args[2].asNumber();
            if (!endResult.isError()) {
                end = ((ANumber) endResult.getValue()).getValue().intValue();
            }
        }
        
        if (args[0] instanceof AList) {
            AList<?> list = (AList<?>) args[0];
            if (end == -1) end = list.size();
            
            StringBuilder sb = new StringBuilder();
            sb.append("Slice [").append(start).append(":").append(end).append("]:\n");
            
            for (int i = start; i < Math.min(end, list.size()); i++) {
                if (i >= 0) {
                    sb.append("[").append(i).append("] ").append(list.get(i).toString()).append("\n");
                }
            }
            
            return new AString(sb.toString());
        }
        
        return new AString("Slice not implemented for this type");
    }
    
    private Value containsValue(Environment context, Value... args) {
        if (args.length < 2) {
            return new AString("Error: contains <data> <value> [col_index]");
        }
        
        String searchValue = args[1].toString();
        
        if (args[0] instanceof AMatrix) {
            AMatrix matrix = (AMatrix) args[0];
            
            if (args.length >= 3) {
                Result<Value> colResult = args[2].asNumber();
                if (colResult.isError()) {
                    return new AString("Error: Invalid column index");
                }
                int col = ((ANumber) colResult.getValue()).getValue().intValue();
                
                for (int i = 0; i < matrix.getRows(); i++) {
                    if (matrix.get(i, col).toString().equals(searchValue)) {
                        return new AString("Found in row " + i);
                    }
                }
                return new AString("Not found");
            } else {
                for (int i = 0; i < matrix.getRows(); i++) {
                    for (int j = 0; j < matrix.getCols(); j++) {
                        if (matrix.get(i, j).toString().equals(searchValue)) {
                            return new AString("Found at row " + i + ", col " + j);
                        }
                    }
                }
                return new AString("Not found");
            }
        } else if (args[0] instanceof AList) {
            AList<?> list = (AList<?>) args[0];
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).toString().equals(searchValue)) {
                    return new AString("Found at index " + i);
                }
            }
            return new AString("Not found");
        }
        
        return new AString("Error: Unsupported data type");
    }
    
    private Value getStats(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide data");
        }
        
        List<BigDecimal> numbers = new ArrayList<>();
        
        if (args[0] instanceof AMatrix) {
            AMatrix matrix = (AMatrix) args[0];
            
            if (args.length >= 2) {
                Result<Value> colResult = args[1].asNumber();
                if (colResult.isError()) {
                    return new AString("Error: Invalid column index");
                }
                int col = ((ANumber) colResult.getValue()).getValue().intValue();
                
                for (int i = 0; i < matrix.getRows(); i++) {
                    Value val = matrix.get(i, col);
                    Result<Value> numResult = val.asNumber();
                    if (!numResult.isError()) {
                        numbers.add(((ANumber) numResult.getValue()).getValue());
                    }
                }
            } else {
                for (int i = 0; i < matrix.getRows(); i++) {
                    for (int j = 0; j < matrix.getCols(); j++) {
                        Value val = matrix.get(i, j);
                        Result<Value> numResult = val.asNumber();
                        if (!numResult.isError()) {
                            numbers.add(((ANumber) numResult.getValue()).getValue());
                        }
                    }
                }
            }
        } else if (args[0] instanceof AList) {
            AList<?> list = (AList<?>) args[0];
            for (int i = 0; i < list.size(); i++) {
                Value val = list.get(i);
                Result<Value> numResult = val.asNumber();
                if (!numResult.isError()) {
                    numbers.add(((ANumber) numResult.getValue()).getValue());
                }
            }
        }
        
        if (numbers.isEmpty()) {
            return new AString("No numeric values found");
        }
        
        numbers.sort(BigDecimal::compareTo);
        
        BigDecimal sum = numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(numbers.size()), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal min = numbers.get(0);
        BigDecimal max = numbers.get(numbers.size() - 1);
        BigDecimal median = numbers.size() % 2 == 0 
            ? numbers.get(numbers.size() / 2 - 1).add(numbers.get(numbers.size() / 2)).divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP)
            : numbers.get(numbers.size() / 2);
        
        StringBuilder sb = new StringBuilder();
        sb.append("Stats (").append(numbers.size()).append(" values):\n");
        sb.append("Min: ").append(min).append("\n");
        sb.append("Max: ").append(max).append("\n");
        sb.append("Mean: ").append(avg).append("\n");
        sb.append("Median: ").append(median).append("\n");
        sb.append("Sum: ").append(sum);
        
        return new AString(sb.toString());
    }
    
    private Value matrixSize(Environment context, Value... args) {
        if (args.length == 0 || !(args[0] instanceof AMatrix)) {
            return new AString("Error: Provide matrix");
        }
        
        AMatrix matrix = (AMatrix) args[0];
        return new AString(matrix.getRows() + " rows, " + matrix.getCols() + " columns");
    }
    
    private Value columnSummary(Environment context, Value... args) {
        if (args.length == 0 || !(args[0] instanceof AMatrix)) {
            return new AString("Error: Provide matrix");
        }
        
        AMatrix matrix = (AMatrix) args[0];
        if (matrix.isEmpty()) {
            return new AString("Matrix is empty");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Columns:\n");
        
        for (int col = 0; col < matrix.getCols(); col++) {
            sb.append(col).append(": ");
            
            List<String> samples = new ArrayList<>();
            for (int row = 0; row < Math.min(3, matrix.getRows()); row++) {
                Value val = matrix.get(row, col);
                if (!(val instanceof ANull)) {
                    samples.add(val.toString());
                }
            }
            
            if (!samples.isEmpty()) {
                sb.append(String.join(", ", samples));
                if (matrix.getRows() > 3) sb.append("...");
            } else {
                sb.append("(all null)");
            }
            sb.append("\n");
        }
        
        return new AString(sb.toString());
    }
    
    private Value sumValues(Environment context, Value... args) {
        if (args.length == 0 || !(args[0] instanceof AMatrix)) {
            return new AString("Error: Provide matrix");
        }
        
        AMatrix matrix = (AMatrix) args[0];
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        
        if (args.length == 1) {
            // Sum entire matrix
            for (int i = 0; i < matrix.getRows(); i++) {
                for (int j = 0; j < matrix.getCols(); j++) {
                    Value val = matrix.get(i, j);
                    Result<Value> numResult = val.asNumber();
                    if (!numResult.isError()) {
                        sum = sum.add(((ANumber) numResult.getValue()).getValue());
                        count++;
                    }
                }
            }
        } else if (args.length == 3) {
            String type = args[1].toString();
            Result<Value> indexResult = args[2].asNumber();
            if (indexResult.isError()) {
                return new AString("Error: Invalid index");
            }
            
            int index = ((ANumber) indexResult.getValue()).getValue().intValue();
            
            if ("row".equals(type)) {
                for (int j = 0; j < matrix.getCols(); j++) {
                    Value val = matrix.get(index, j);
                    Result<Value> numResult = val.asNumber();
                    if (!numResult.isError()) {
                        sum = sum.add(((ANumber) numResult.getValue()).getValue());
                        count++;
                    }
                }
            } else if ("col".equals(type)) {
                for (int i = 0; i < matrix.getRows(); i++) {
                    Value val = matrix.get(i, index);
                    Result<Value> numResult = val.asNumber();
                    if (!numResult.isError()) {
                        sum = sum.add(((ANumber) numResult.getValue()).getValue());
                        count++;
                    }
                }
            }
        }
        
        return new AString("Sum: " + sum + " (" + count + " numeric values)");
    }
    
    private Value countValues(Environment context, Value... args) {
        if (args.length == 0 || !(args[0] instanceof AMatrix)) {
            return new AString("Error: Provide matrix");
        }
        
        AMatrix matrix = (AMatrix) args[0];
        int nonNull = 0;
        int total = 0;
        
        if (args.length == 1) {
            total = matrix.getRows() * matrix.getCols();
            for (int i = 0; i < matrix.getRows(); i++) {
                for (int j = 0; j < matrix.getCols(); j++) {
                    Value val = matrix.get(i, j);
                    if (!(val instanceof ANull)) {
                        nonNull++;
                    }
                }
            }
        } else if (args.length == 3) {
            String type = args[1].toString();
            Result<Value> indexResult = args[2].asNumber();
            if (indexResult.isError()) {
                return new AString("Error: Invalid index");
            }
            
            int index = ((ANumber) indexResult.getValue()).getValue().intValue();
            
            if ("row".equals(type)) {
                total = matrix.getCols();
                for (int j = 0; j < matrix.getCols(); j++) {
                    Value val = matrix.get(index, j);
                    if (!(val instanceof ANull)) {
                        nonNull++;
                    }
                }
            } else if ("col".equals(type)) {
                total = matrix.getRows();
                for (int i = 0; i < matrix.getRows(); i++) {
                    Value val = matrix.get(i, index);
                    if (!(val instanceof ANull)) {
                        nonNull++;
                    }
                }
            }
        }
        
        return new AString(nonNull + "/" + total + " non-null values");
    }
    
    private Value uniqueValues(Environment context, Value... args) {
        if (args.length != 2 || !(args[0] instanceof AMatrix)) {
            return new AString("Error: unique <matrix> <col_index>");
        }
        
        AMatrix matrix = (AMatrix) args[0];
        Result<Value> colResult = args[1].asNumber();
        if (colResult.isError()) {
            return new AString("Error: Invalid column index");
        }
        
        int col = ((ANumber) colResult.getValue()).getValue().intValue();
        Map<String, Integer> counts = new HashMap<>();
        
        for (int i = 0; i < matrix.getRows(); i++) {
            Value val = matrix.get(i, col);
            String str = val.toString();
            counts.put(str, counts.getOrDefault(str, 0) + 1);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Column ").append(col).append(" has ").append(counts.size()).append(" unique values:\n");
        counts.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .limit(10)
            .forEach(entry -> sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));
        
        if (counts.size() > 10) {
            sb.append("... (").append(counts.size() - 10).append(" more)");
        }
        
        return new AString(sb.toString());
    }
    
    private Value findRows(Environment context, Value... args) {
        if (args.length != 3 || !(args[0] instanceof AMatrix)) {
            return new AString("Error: find <matrix> <col_index> <value>");
        }
        
        AMatrix matrix = (AMatrix) args[0];
        Result<Value> colResult = args[1].asNumber();
        if (colResult.isError()) {
            return new AString("Error: Invalid column index");
        }
        
        int col = ((ANumber) colResult.getValue()).getValue().intValue();
        String searchValue = args[2].toString();
        
        List<Integer> matches = new ArrayList<>();
        for (int i = 0; i < matrix.getRows(); i++) {
            Value val = matrix.get(i, col);
            if (val.toString().equals(searchValue)) {
                matches.add(i);
            }
        }
        
        if (matches.isEmpty()) {
            return new AString("No matches found");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(matches.size()).append(" matches in rows: ");
        for (int i = 0; i < Math.min(10, matches.size()); i++) {
            sb.append(matches.get(i));
            if (i < Math.min(10, matches.size()) - 1) sb.append(", ");
        }
        if (matches.size() > 10) {
            sb.append("... (").append(matches.size() - 10).append(" more)");
        }
        
        return new AString(sb.toString());
    }

    public void registerCommand(String name, Command command) {
        commands.put(name, command);
    }

    public Command getCommand(String name) {
        return commands.get(name);
    }

    public boolean hasCommand(String name) {
        return commands.containsKey(name);
    }

    public java.util.Set<String> getCommandNames() {
        return commands.keySet();
    }

    public java.util.Map<String, Command> getAllCommands() {
        return new java.util.HashMap<>(commands);
    }

	public boolean exists(String commandName) {
		return commands.get(commandName) != null;
	}
}