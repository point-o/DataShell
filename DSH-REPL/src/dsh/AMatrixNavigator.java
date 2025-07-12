package dsh;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

/**
 * A navigator class for efficiently traversing and extracting data from AMatrix
 * Handles null values, headers, and common data extraction patterns
 * 
 * @author Ryan Pointer
 * @version 7/10/25
 */
public class AMatrixNavigator {
    private AMatrix matrix;
    private Map<String, Integer> headerMap;
    protected boolean hasHeaders;
    protected int currentRow;
    protected int currentCol;
    private int dataStartRow;
    
    /**
     * Constructor for matrix with headers
     */
    public AMatrixNavigator(AMatrix matrix, boolean hasHeaders) {
        this.matrix = matrix;
        this.hasHeaders = hasHeaders;
        this.currentRow = 0;
        this.currentCol = 0;
        this.dataStartRow = hasHeaders ? 1 : 0;
        
        if (hasHeaders) {
            buildHeaderMap();
        }
    }
    
    /**
     * Constructor assuming no headers
     */
    public AMatrixNavigator(AMatrix matrix) {
        this(matrix, false);
    }
    
    /**
     * Build a map of column headers to their indices
     */
    private void buildHeaderMap() {
        headerMap = new HashMap<>();
        if (matrix.getRows() > 0) {
            for (int col = 0; col < matrix.getCols(); col++) {
                Value headerValue = matrix.get(0, col);
                if (headerValue != null && !headerValue.type().equals("null")) {
                    headerMap.put(headerValue.toString().trim(), col);
                }
            }
        }
    }
    
    /**
     * Get column index by header name
     */
    public int getColumnIndex(String headerName) {
        if (!hasHeaders || headerMap == null) {
            return -1;
        }
        return headerMap.getOrDefault(headerName, -1);
    }
    
    /**
     * Get all header names
     */
    public Set<String> getHeaders() {
        if (!hasHeaders || headerMap == null) {
            return new HashSet<>();
        }
        return new HashSet<>(headerMap.keySet());
    }
    
    /**
     * Move to next non-null value in current row
     */
    public boolean nextNonNull() {
        while (currentCol < matrix.getCols()) {
            Value value = matrix.get(currentRow, currentCol);
            if (value != null && !value.type().equals("null")) {
                return true;
            }
            currentCol++;
        }
        return false;
    }
    
    /**
     * Move to next row with data (skipping nulls)
     */
    public boolean nextDataRow() {
        currentRow++;
        currentCol = 0;
        
        while (currentRow < matrix.getRows()) {
            // Check if row has any non-null data
            for (int col = 0; col < matrix.getCols(); col++) {
                Value value = matrix.get(currentRow, col);
                if (value != null && !value.type().equals("null")) {
                    return true;
                }
            }
            currentRow++;
        }
        return false;
    }
    
    /**
     * Reset navigator to start of data
     */
    public void reset() {
        currentRow = dataStartRow;
        currentCol = 0;
    }
    
    /**
     * Get current value
     */
    public Value getCurrentValue() {
        if (currentRow >= 0 && currentRow < matrix.getRows() && 
            currentCol >= 0 && currentCol < matrix.getCols()) {
            return matrix.get(currentRow, currentCol);
        }
        return new ANull();
    }
    
    /**
     * Get value by header name for current row
     */
    public Value getValueByHeader(String headerName) {
        int colIndex = getColumnIndex(headerName);
        if (colIndex >= 0 && currentRow >= dataStartRow && currentRow < matrix.getRows()) {
            return matrix.get(currentRow, colIndex);
        }
        return new ANull();
    }
    
    /**
     * Extract entire column by header name (excluding nulls)
     */
    public AList<Value> getColumnByHeader(String headerName) {
        int colIndex = getColumnIndex(headerName);
        if (colIndex < 0) {
            return new AList<>();
        }
        
        List<Value> values = new ArrayList<>();
        for (int row = dataStartRow; row < matrix.getRows(); row++) {
            Value value = matrix.get(row, colIndex);
            if (value != null && !value.type().equals("null")) {
                values.add(value);
            }
        }
        return new AList<>(values);
    }
    
    /**
     * Extract entire column by index (excluding nulls)
     */
    public AList<Value> getColumnByIndex(int colIndex) {
        if (colIndex < 0 || colIndex >= matrix.getCols()) {
            return new AList<>();
        }
        
        List<Value> values = new ArrayList<>();
        for (int row = dataStartRow; row < matrix.getRows(); row++) {
            Value value = matrix.get(row, colIndex);
            if (value != null && !value.type().equals("null")) {
                values.add(value);
            }
        }
        return new AList<>(values);
    }
    
    /**
     * Find all rows where a column matches a condition
     */
    public AList<Value> findRowsWhere(String headerName, Predicate<Value> condition) {
        int colIndex = getColumnIndex(headerName);
        if (colIndex < 0) {
            return new AList<>();
        }
        
        List<Value> matchingRows = new ArrayList<>();
        for (int row = dataStartRow; row < matrix.getRows(); row++) {
            Value value = matrix.get(row, colIndex);
            if (value != null && !value.type().equals("null") && condition.test(value)) {
                matchingRows.add(new ANumber(new BigDecimal(row)));
            }
        }
        
        return new AList<>(matchingRows);
    }
    
    /**
     * Get summary statistics for a numeric column
     */
    public Map<String, Value> getNumericSummary(String headerName) {
        AList<Value> values = getColumnByHeader(headerName);
        Map<String, Value> summary = new HashMap<>();
        
        if (values.isEmpty()) {
            return summary;
        }
        
        List<BigDecimal> numbers = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            Value value = values.get(i);
            if (value.type().equals("number")) {
                numbers.add((BigDecimal) value.getValue());
            }
        }
        
        if (numbers.isEmpty()) {
            return summary;
        }
        
        // Calculate statistics
        BigDecimal sum = numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal count = new BigDecimal(numbers.size());
        @SuppressWarnings("deprecation")
		BigDecimal avg = sum.divide(count, 10, BigDecimal.ROUND_HALF_UP);
        
        Collections.sort(numbers);
        BigDecimal min = numbers.get(0);
        BigDecimal max = numbers.get(numbers.size() - 1);
        @SuppressWarnings("deprecation")
		BigDecimal median = numbers.size() % 2 == 0 ? 
            numbers.get(numbers.size() / 2 - 1).add(numbers.get(numbers.size() / 2))
                .divide(new BigDecimal(2), 10, BigDecimal.ROUND_HALF_UP) :
            numbers.get(numbers.size() / 2);
        
        summary.put("count", new ANumber(count));
        summary.put("sum", new ANumber(sum));
        summary.put("average", new ANumber(avg));
        summary.put("min", new ANumber(min));
        summary.put("max", new ANumber(max));
        summary.put("median", new ANumber(median));
        
        return summary;
    }
    
    /**
     * Get frequency count for a column
     */
    public Map<String, Value> getFrequencyCount(String headerName) {
        AList<Value> values = getColumnByHeader(headerName);
        Map<String, Integer> counts = new HashMap<>();
        
        for (int i = 0; i < values.size(); i++) {
            String key = values.get(i).toString();
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }
        
        Map<String, Value> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            result.put(entry.getKey(), new ANumber(new BigDecimal(entry.getValue())));
        }
        
        return result;
    }
    
    /**
     * Filter rows based on multiple column conditions
     */
    public AList<AList<Value>> filterRows(Map<String, Predicate<Value>> conditions) {
        List<AList<Value>> filteredRows = new ArrayList<>();
        
        for (int row = dataStartRow; row < matrix.getRows(); row++) {
            boolean matchesAll = true;
            
            for (Map.Entry<String, Predicate<Value>> condition : conditions.entrySet()) {
                Value value = getValueByRowAndHeader(row, condition.getKey());
                if (value == null || value.type().equals("null") || !condition.getValue().test(value)) {
                    matchesAll = false;
                    break;
                }
            }
            
            if (matchesAll) {
                List<Value> rowData = new ArrayList<>();
                for (int col = 0; col < matrix.getCols(); col++) {
                    rowData.add(matrix.get(row, col));
                }
                filteredRows.add(new AList<>(rowData));
            }
        }
        
        return new AList<>(filteredRows);
    }
    
    /**
     * Get value by row and header name
     */
    private Value getValueByRowAndHeader(int row, String headerName) {
        int colIndex = getColumnIndex(headerName);
        if (colIndex >= 0 && row >= 0 && row < matrix.getRows()) {
            return matrix.get(row, colIndex);
        }
        return new ANull();
    }
    
    /**
     * Get distinct values from a column
     */
    public AList<Value> getDistinctValues(String headerName) {
        AList<Value> values = getColumnByHeader(headerName);
        Set<String> seen = new HashSet<>();
        List<Value> distinct = new ArrayList<>();
        
        for (int i = 0; i < values.size(); i++) {
            Value value = values.get(i);
            String key = value.toString();
            if (!seen.contains(key)) {
                seen.add(key);
                distinct.add(value);
            }
        }
        
        return new AList<>(distinct);
    }
    
    /**
     * Group rows by column value
     */
    public Map<String, AList<AList<Value>>> groupBy(String headerName) {
        Map<String, List<AList<Value>>> groups = new HashMap<>();
        
        for (int row = dataStartRow; row < matrix.getRows(); row++) {
            Value groupValue = getValueByRowAndHeader(row, headerName);
            if (groupValue == null || groupValue.type().equals("null")) {
                continue;
            }
            
            String groupKey = groupValue.toString();
            groups.computeIfAbsent(groupKey, k -> new ArrayList<>());
            
            List<Value> rowData = new ArrayList<>();
            for (int col = 0; col < matrix.getCols(); col++) {
                rowData.add(matrix.get(row, col));
            }
            groups.get(groupKey).add(new AList<>(rowData));
        }
        
        Map<String, AList<AList<Value>>> result = new HashMap<>();
        for (Map.Entry<String, List<AList<Value>>> entry : groups.entrySet()) {
            result.put(entry.getKey(), new AList<>(entry.getValue()));
        }
        
        return result;
    }
    
    /**
     * Get current position
     */
    public int getCurrentRow() {
        return currentRow;
    }
    
    public int getCurrentCol() {
        return currentCol;
    }
    
    /**
     * Check if there are more rows to process
     */
    public boolean hasMoreRows() {
        return currentRow < matrix.getRows();
    }
    
    /**
     * Check if current position has data
     */
    public boolean hasData() {
        Value value = getCurrentValue();
        return value != null && !value.type().equals("null");
    }
}