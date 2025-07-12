package dsh;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;

public class AMatrixNavigatorTest {
    private AMatrix testMatrix;
    private AMatrixNavigator navigator;
    private AMatrixNavigator noHeaderNavigator;
    private AMatrix emptyMatrix;
    private AMatrixNavigator emptyNavigator;

    @Before
    public void setUp() {
        // Setup test matrix with headers
        testMatrix = new AMatrix();
        testMatrix.addRow(Arrays.asList(
            new AString("Name"), 
            new AString("Age"), 
            new AString("Score")));
        testMatrix.addRow(Arrays.asList(
            new AString("Alice"), 
            new ANumber(25), 
            new ANumber(85)));
        testMatrix.addRow(Arrays.asList(
            new AString("Bob"), 
            new ANumber(30), 
            new ANumber(92)));
        testMatrix.addRow(Arrays.asList(
            new AString("Charlie"), 
            new ANull(), 
            new ANumber(78)));

        navigator = new AMatrixNavigator(testMatrix, true);
        noHeaderNavigator = new AMatrixNavigator(testMatrix);

        // Setup empty matrix
        emptyMatrix = new AMatrix();
        emptyNavigator = new AMatrixNavigator(emptyMatrix);
    }

    @Test
    public void testConstructorWithHeaders() {
        assertEquals(1, navigator.getCurrentRow());
        assertEquals(0, navigator.getCurrentCol());
        assertTrue(navigator.hasHeaders);
    }

    @Test
    public void testConstructorWithoutHeaders() {
        assertEquals(0, noHeaderNavigator.getCurrentRow());
        assertEquals(0, noHeaderNavigator.getCurrentCol());
        assertFalse(noHeaderNavigator.hasHeaders);
    }

    @Test
    public void testGetHeaders() {
        Set<String> expected = new HashSet<>(Arrays.asList("Name", "Age", "Score"));
        assertEquals(expected, navigator.getHeaders());
        assertTrue(emptyNavigator.getHeaders().isEmpty());
    }

    @Test
    public void testGetColumnIndex() {
        assertEquals(1, navigator.getColumnIndex("Age"));
        assertEquals(-1, navigator.getColumnIndex("Nonexistent"));
        assertEquals(-1, noHeaderNavigator.getColumnIndex("Name"));
    }

    @Test
    public void testNextNonNull() {
        navigator.reset();
        assertTrue(navigator.nextNonNull());
        assertEquals("Alice", navigator.getCurrentValue().toString());

        navigator.currentCol = 2; // Move to last column
        assertFalse(navigator.nextNonNull()); // Should fail as no more columns
    }

    @Test
    public void testNextDataRow() {
        assertTrue(navigator.nextDataRow());
        assertEquals(2, navigator.getCurrentRow());

        navigator.currentRow = testMatrix.getRows() - 1;
        assertFalse(navigator.nextDataRow());
    }

    @Test
    public void testGetCurrentValue() {
        navigator.reset();
        assertEquals("Alice", navigator.getCurrentValue().toString());

        // Test out of bounds
        navigator.currentRow = 100;
        assertTrue(navigator.getCurrentValue() instanceof ANull);
    }

    @Test
    public void testGetValueByHeader() {
        assertEquals("Alice", navigator.getValueByHeader("Name").toString());
        assertTrue(navigator.getValueByHeader("Nonexistent") instanceof ANull);

        // Test before data start row
        navigator.currentRow = 0;
        assertTrue(navigator.getValueByHeader("Name") instanceof ANull);
    }

    @Test
    public void testGetColumnByHeader() {
        AList<Value> ages = navigator.getColumnByHeader("Age");
        assertEquals(2, ages.size());
        assertEquals("25", ages.get(0).toString());
        assertEquals("30", ages.get(1).toString());

        assertTrue(navigator.getColumnByHeader("Nonexistent").isEmpty());
    }

    @Test
    public void testGetColumnByIndex() {
        AList<Value> names = navigator.getColumnByIndex(0);
        assertEquals(3, names.size());
        assertEquals("Alice", names.get(0).toString());

        assertTrue(navigator.getColumnByIndex(-1).isEmpty());
        assertTrue(navigator.getColumnByIndex(100).isEmpty());
    }

    @Test
    public void testFindRowsWhere() {
        Predicate<Value> highScore = v ->
            ((ANumber) v).getValue().compareTo(new BigDecimal(90)) >= 0;

        AList<Value> result = navigator.findRowsWhere("Score", highScore);
        assertEquals(1, result.size());
        assertEquals("2", result.get(0).toString()); // Bob's row index

        // Test with non-existent column
        assertTrue(navigator.findRowsWhere("Nonexistent", highScore).isEmpty());
    }

    @Test
    public void testGetNumericSummary() {
        Map<String, Value> summary = navigator.getNumericSummary("Age");
        assertEquals("2", summary.get("count").toString());
        assertEquals("55", summary.get("sum").toString());
        assertEquals("27.5", summary.get("average").toString());
        assertEquals("25", summary.get("min").toString());
        assertEquals("30", summary.get("max").toString());

        // Test with non-numeric column
        assertTrue(navigator.getNumericSummary("Name").isEmpty());
    }

    @Test
    public void testGetFrequencyCount() {
        Map<String, Value> counts = navigator.getFrequencyCount("Name");
        assertEquals("1", counts.get("Alice").toString());
        assertEquals("1", counts.get("Bob").toString());
        assertEquals("1", counts.get("Charlie").toString());
    }

    @Test
    public void testFilterRows() {
        Map<String, Predicate<Value>> filters = new HashMap<>();
        filters.put("Score", v -> ((ANumber) v).getValue().compareTo(new BigDecimal(80)) >= 0);

        AList<AList<Value>> result = navigator.filterRows(filters);
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).toString());
        assertEquals("Bob", result.get(1).toString());

        // Test with empty conditions
        assertTrue(navigator.filterRows(new HashMap<>()).isEmpty());
    }

    @Test
    public void testGetDistinctValues() {
        AList<Value> distinctNames = navigator.getDistinctValues("Name");
        assertEquals(3, distinctNames.size());

        // Add duplicate name
        testMatrix.addRow(Arrays.asList(
            new AString("Alice"), 
            new ANumber(28), 
            new ANumber(88)));

        distinctNames = navigator.getDistinctValues("Name");
        assertEquals(3, distinctNames.size()); // Still 3 unique names
    }

    @Test
    public void testGroupBy() {
        Map<String, AList<AList<Value>>> groups = navigator.groupBy("Name");
        assertEquals(3, groups.size());
        assertEquals(1, groups.get("Alice").size());
        assertEquals("Alice", groups.get("Alice").get(0).toString());

        // Test with non-existent column
        assertTrue(navigator.groupBy("Nonexistent").isEmpty());
    }

    @Test
    public void testEmptyMatrix() {
        assertTrue(emptyNavigator.getHeaders().isEmpty());
        assertFalse(emptyNavigator.nextDataRow());
        assertTrue(emptyNavigator.getColumnByIndex(0).isEmpty());
    }

    @Test
    public void testHasMoreRows() {
        navigator.reset();
        assertTrue(navigator.hasMoreRows());

        navigator.currentRow = testMatrix.getRows();
        assertFalse(navigator.hasMoreRows());
    }

    @Test
    public void testHasData() {
        navigator.reset();
        assertTrue(navigator.hasData());

        navigator.currentCol = 100; // Move beyond columns
        assertFalse(navigator.hasData());
    }
}
