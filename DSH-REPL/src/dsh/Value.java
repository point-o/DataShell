package dsh;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.ArrayList;

/**
 * A class for types, with result based safety.
 * 
 * @author Ryan Pointer
 * @version 7/12/25
 */
public interface Value {
    // defined per class
    Result<Value> asNumber();
    Result<Value> asString();
    Result<Value> asBoolean();
    String type();
    String toString();
    Object getValue();
    
    public default Result<Value> add(Value other) {
    	// likely broadcasts
        if (this.type().equals("list")) {
            return applyListOperation(this, other, (a, b) -> a.add(b));
        }
        
        if (this.type().equals("matrix")) {
            return applyMatrixOperation(this, other, (a, b) -> a.add(b));
        }
        
        if (other.type().equals("list")) {
            // right-side is treated as 0
            return this.add(new ANumber(BigDecimal.ZERO));
        }
        
        if (other.type().equals("matrix")) {
            // right-side is treated as 0
            return this.add(new ANumber(BigDecimal.ZERO));
        }
        
        if (this.type().equals("string") || other.type().equals("string")) {
            return Result.ok(new AString(this.toString() + other.toString()));
        }
        
        return this.asNumber()
            .flatMap(left -> other.asNumber() // literally just nested ifs (handled by Result)
                .map(right -> { 
                    BigDecimal leftNum = (BigDecimal) left.getValue();
                    BigDecimal rightNum = (BigDecimal) right.getValue();
                    return new ANumber(leftNum.add(rightNum));
                }));
    }
    
    public default Result<Value> subtract(Value other) {
        if (this.type().equals("list")) {
            return applyListOperation(this, other, (a, b) -> a.subtract(b));
        }
        
        if (this.type().equals("matrix")) {
            return applyMatrixOperation(this, other, (a, b) -> a.subtract(b));
        }
        
        if (other.type().equals("list")) {
            return this.subtract(new ANumber(BigDecimal.ZERO));
        }
        
        if (other.type().equals("matrix")) {
            return this.subtract(new ANumber(BigDecimal.ZERO));
        }
        
        return this.asNumber()
            .flatMap(left -> other.asNumber()
                .map(right -> {
                    BigDecimal leftNum = (BigDecimal) left.getValue();
                    BigDecimal rightNum = (BigDecimal) right.getValue();
                    return new ANumber(leftNum.subtract(rightNum));
                }));
    }
    
    public default Result<Value> multiply(Value other) {
        if (this.type().equals("list")) {
            return applyListOperation(this, other, (a, b) -> a.multiply(b));
        }
        
        if (this.type().equals("matrix")) {
            return applyMatrixOperation(this, other, (a, b) -> a.multiply(b));
        }
        
        if (other.type().equals("list")) {
            return this.multiply(new ANumber(BigDecimal.ONE));
        }
        
        if (other.type().equals("matrix")) {
            return this.multiply(new ANumber(BigDecimal.ONE));
        }
        
        return this.asNumber()
            .flatMap(left -> other.asNumber()
                .map(right -> {
                    BigDecimal leftNum = (BigDecimal) left.getValue();
                    BigDecimal rightNum = (BigDecimal) right.getValue();
                    return new ANumber(leftNum.multiply(rightNum));
                }));
    }
    
    public default Result<Value> divide(Value other) {
        if (this.type().equals("list")) {
            return applyListOperation(this, other, (a, b) -> a.divide(b));
        }
        
        if (this.type().equals("matrix")) {
            return applyMatrixOperation(this, other, (a, b) -> a.divide(b));
        }
        
        if (other.type().equals("list")) {
            // one, because dividing by zero is bad
            return this.divide(new ANumber(BigDecimal.ONE));
        }
        
        if (other.type().equals("matrix")) {
            // one, because dividing by zero is bad
            return this.divide(new ANumber(BigDecimal.ONE));
        }
        
        return this.asNumber()
            .flatMap(left -> other.asNumber()
                .flatMap(right -> {
                    BigDecimal leftNum = (BigDecimal) left.getValue();
                    BigDecimal rightNum = (BigDecimal) right.getValue();
                    
                    if (rightNum.compareTo(BigDecimal.ZERO) == 0) {
                        return Result.error(Result.ErrorType.ARITHMETIC, "Division by zero");
                    }
                    
                    try {
                        BigDecimal result = leftNum.divide(rightNum, MathContext.DECIMAL128);
                        return Result.ok(new ANumber(result));
                    } catch (ArithmeticException e) {
                        return Result.error(Result.ErrorType.ARITHMETIC, "Division error: " + e.getMessage(), e);
                    }
                }));
    }
    
    public default Result<Value> modulo(Value other) {
        if (this.type().equals("list")) {
            return applyListOperation(this, other, (a, b) -> a.modulo(b));
        }
        
        if (this.type().equals("matrix")) {
            return applyMatrixOperation(this, other, (a, b) -> a.modulo(b));
        }
        
        if (other.type().equals("list")) {
            return this.modulo(new ANumber(BigDecimal.ONE));
        }
        
        if (other.type().equals("matrix")) {
            return this.modulo(new ANumber(BigDecimal.ONE));
        }
        
        return this.asNumber()
            .flatMap(left -> other.asNumber()
                .flatMap(right -> {
                    BigDecimal leftNum = (BigDecimal) left.getValue();
                    BigDecimal rightNum = (BigDecimal) right.getValue();
                    
                    if (rightNum.compareTo(BigDecimal.ZERO) == 0) {
                        return Result.error(Result.ErrorType.ARITHMETIC, "Modulus by zero");
                    }
                    
                    try {
                        BigDecimal result = leftNum.remainder(rightNum);
                        return Result.ok(new ANumber(result));
                    } catch (ArithmeticException e) {
                        return Result.error(Result.ErrorType.ARITHMETIC, "Modulus error: " + e.getMessage(), e);
                    }
                }));
    }
    
    public default Result<Value> power(Value other) {
        if (this.type().equals("list")) {
            return applyListOperation(this, other, (a, b) -> a.power(b));
        }
        
        if (this.type().equals("matrix")) {
            return applyMatrixOperation(this, other, (a, b) -> a.power(b));
        }
        
        if (other.type().equals("list")) {
            return this.power(new ANumber(BigDecimal.ONE));
        }
        
        if (other.type().equals("matrix")) {
            return this.power(new ANumber(BigDecimal.ONE));
        }
        
        return this.asNumber()
            .flatMap(left -> other.asNumber() // fancy nested ifs handled by Result
                .flatMap(right -> {
                    BigDecimal base = (BigDecimal) left.getValue();
                    BigDecimal expo = (BigDecimal) right.getValue();
                    if (base.compareTo(BigDecimal.ZERO) == 0 && expo.compareTo(BigDecimal.ZERO) == 0) {
                        return Result.error(Result.ErrorType.ARITHMETIC, "0^0 is undefined");
                    }
                    try {
                        BigDecimal result;
                        if (expo.stripTrailingZeros().scale() <= 0) {
                            int e = expo.intValueExact();
                            if (e >= 0) {
                                result = base.pow(e, MathContext.DECIMAL128);
                            } else {
                                result = BigDecimal.ONE
                                    .divide(base.pow(-e, MathContext.DECIMAL128), MathContext.DECIMAL128);
                            }
                        } else {
                            // if fraction fall back to double precision (duh)
                            double d = Math.pow(base.doubleValue(), expo.doubleValue());
                            if (Double.isNaN(d) || Double.isInfinite(d)) {
                                return Result.error(Result.ErrorType.ARITHMETIC, "Power operation resulted in NaN or Infinity");
                            }
                            result = BigDecimal.valueOf(d);
                        }
                        return Result.ok(new ANumber(result));
                    } catch (ArithmeticException | NumberFormatException e) {
                        return Result.error(Result.ErrorType.ARITHMETIC, "Power operation failed: " + e.getMessage(), e);
                    }
                }));
    }
    
    // normal list dispatcher
    static Result<Value> applyListOperation(Value left, Value right, BinaryOperator op) {
        try {
            if (!left.type().equals("list")) {
                return Result.error(Result.ErrorType.RUNTIME, "Internal error: left operand is not a list");
            }
            
            @SuppressWarnings("unchecked")
            List<Value> leftList = (List<Value>) left.getValue();
            
            // this should've been prevented, but you never know
            if (leftList.size() > 10000) {
                return Result.error(Result.ErrorType.VALIDATION, "List operation would create too many elements (max 10000)");
            }
            
            // list-on-list broadcasts
            if (right.type().equals("list")) {
                return applyListOnListOperation(leftList, right, op);
            } else {
                return applyBroadcastOperation(leftList, right, op);
            }
            
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, "Unexpected error in list operation: " + e.getMessage(), e);
        }
    }
    
    // matrix operation dispatcher
    static Result<Value> applyMatrixOperation(Value left, Value right, BinaryOperator op) {
        try {
            if (!left.type().equals("matrix")) {
                return Result.error(Result.ErrorType.RUNTIME, "Internal error: left operand is not a matrix");
            }

            @SuppressWarnings("unchecked")
			List<List<Value>> leftMatrix = (List<List<Value>>) left.getValue();
            
            // again should've been prevented
            int totalElements = leftMatrix.size() * (leftMatrix.isEmpty() ? 0 : leftMatrix.get(0).size());
            if (totalElements > 10000) {
                return Result.error(Result.ErrorType.VALIDATION, "Matrix operation would create too many elements (max 10000)");
            }
            
            if (right.type().equals("matrix")) {
                return applyMatrixOnMatrixOperation(leftMatrix, right, op);
            } else {
                return applyMatrixBroadcastOperation(leftMatrix, right, op);
            }
            
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, "Unexpected error in matrix operation: " + e.getMessage(), e);
        }
    }

    static Result<Value> applyListOnListOperation(List<Value> leftList, Value right, BinaryOperator op) {
        try {
            @SuppressWarnings("unchecked")
            List<Value> rightList = (List<Value>) right.getValue();
            List<Value> result = new ArrayList<>();
            
            int leftSize = leftList.size();
            int rightSize = rightList.size();
            int minSize = Math.min(leftSize, rightSize);
            
            for (int i = 0; i < minSize; i++) {
                Result<Value> elementResult = op.apply(leftList.get(i), rightList.get(i));
                if (elementResult.isError()) {
                    return Result.error(Result.ErrorType.RUNTIME, 
                        "List operation failed at index " + i + ": " + elementResult.getErrorMessage(),
                        elementResult.getCause());
                }
                result.add(elementResult.getValue());
            }
            
            if (leftSize > minSize) {
                result.addAll(leftList.subList(minSize, leftSize));
            } else if (rightSize > minSize) {
                result.addAll(rightList.subList(minSize, rightSize));
            }
            
            return Result.ok(new AList<>(result));
            
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, "Unexpected error in list-on-list operation: " + e.getMessage(), e);
        }
    }
    
    static Result<Value> applyBroadcastOperation(List<Value> leftList, Value right, BinaryOperator op) {
        try {
            List<Value> result = new ArrayList<>();
            
            for (int i = 0; i < leftList.size(); i++) {
                Result<Value> elementResult = op.apply(leftList.get(i), right);
                if (elementResult.isError()) {
                    return Result.error(Result.ErrorType.RUNTIME, 
                        "List broadcast operation failed at index " + i + ": " + elementResult.getErrorMessage(),
                        elementResult.getCause());
                }
                result.add(elementResult.getValue());
            }
            
            return Result.ok(new AList<>(result));
            
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, "Unexpected error in broadcast operation: " + e.getMessage(), e);
        }
    }
    
    static Result<Value> applyMatrixOnMatrixOperation(List<List<Value>> leftMatrix, Value right, BinaryOperator op) {
        try {
            @SuppressWarnings("unchecked")
            List<List<Value>> rightMatrix = (List<List<Value>>) right.getValue();
            List<List<Value>> result = new ArrayList<>();
            
            int leftRows = leftMatrix.size();
            int rightRows = rightMatrix.size();
            int leftCols = leftMatrix.isEmpty() ? 0 : leftMatrix.get(0).size();
            int rightCols = rightMatrix.isEmpty() ? 0 : rightMatrix.get(0).size();
            
            int minRows = Math.min(leftRows, rightRows);
            int minCols = Math.min(leftCols, rightCols);
            
            // Apply operation to overlapping elements
            for (int i = 0; i < minRows; i++) {
                List<Value> resultRow = new ArrayList<>();
                
                for (int j = 0; j < minCols; j++) {
                    Result<Value> elementResult = op.apply(leftMatrix.get(i).get(j), rightMatrix.get(i).get(j));
                    if (elementResult.isError()) {
                        return Result.error(Result.ErrorType.RUNTIME, 
                            "Matrix operation failed at position [" + i + "," + j + "]: " + elementResult.getErrorMessage(),
                            elementResult.getCause());
                    }
                    resultRow.add(elementResult.getValue());
                }
                
                if (leftCols > minCols) {
                    resultRow.addAll(leftMatrix.get(i).subList(minCols, leftCols));
                } else if (rightCols > minCols) {
                    resultRow.addAll(rightMatrix.get(i).subList(minCols, rightCols));
                }
                
                result.add(resultRow);
            }
            
            if (leftRows > minRows) {
                for (int i = minRows; i < leftRows; i++) {
                    result.add(new ArrayList<>(leftMatrix.get(i)));
                }
            } else if (rightRows > minRows) {
                for (int i = minRows; i < rightRows; i++) {
                    result.add(new ArrayList<>(rightMatrix.get(i)));
                }
            }
            
            return Result.ok(new AMatrix(result));
            
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, "Unexpected error in matrix-on-matrix operation: " + e.getMessage(), e);
        }
    }
    
    static Result<Value> applyMatrixBroadcastOperation(List<List<Value>> leftMatrix, Value right, BinaryOperator op) {
        try {
            List<List<Value>> result = new ArrayList<>();
            
            for (int i = 0; i < leftMatrix.size(); i++) {
                List<Value> resultRow = new ArrayList<>();
                for (int j = 0; j < leftMatrix.get(i).size(); j++) {
                    Result<Value> elementResult = op.apply(leftMatrix.get(i).get(j), right);
                    if (elementResult.isError()) {
                        return Result.error(Result.ErrorType.RUNTIME, 
                            "Matrix broadcast operation failed at position [" + i + "," + j + "]: " + elementResult.getErrorMessage(),
                            elementResult.getCause());
                    }
                    resultRow.add(elementResult.getValue());
                }
                result.add(resultRow);
            }
            
            return Result.ok(new AMatrix(result));
            
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, "Unexpected error in matrix broadcast operation: " + e.getMessage(), e);
        }
    }
    
    @FunctionalInterface
    interface BinaryOperator {
        Result<Value> apply(Value a, Value b);
    }
}