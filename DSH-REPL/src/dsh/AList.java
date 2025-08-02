package dsh;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to represent a homogeneous List of Value elements.
 * All elements must be of the same runtime type extending Value.
 * 
 * @author Ryan Pointer
 * @version 8/2/25
 */
public class AList<T extends Value> implements Value {
    private List<T> elements;
    private Class<?> elementType; // Enforces homogeneous type at runtime

    public AList() {
        this.elements = new ArrayList<>();
        this.elementType = null;
    }

    public AList(List<T> elements) {
        this();
        for (T el : elements) {
            put(el); // Enforce type while adding
        }
    }

    @Override
    public Result<Value> asNumber() {
        return Result.ok(new ANumber(new BigDecimal(elements.size())));
    }

    @Override
    public Result<Value> asString() {
        try {
        	int printedSize = 9;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < printedSize && i < elements.size(); ++i) {
                Value element = elements.get(i);
                if (element instanceof ANumber) {
                    BigDecimal num = ((ANumber) element).getValue();
                    sb.append(num.stripTrailingZeros().toPlainString());
                } else if (element instanceof AList || element instanceof AString) { // nested lists in the future? matrix is implemented but i dunno
                    sb.append(element.asString());
                } else {
                    sb.append(element.toString()); // fallback
                }
                
                if (i < elements.size() - 1) {
                    sb.append(", ");
                }
            }
            if (printedSize < elements.size()) {
            	sb.append("..., ");
            	BigDecimal lastNum = ((ANumber)elements.get(elements.size()-1)).getValue();
            	sb.append(lastNum.stripTrailingZeros().toPlainString());
            }
            	
            return Result.ok(new AString(sb.toString()));
        } catch (Exception e) {
            return Result.error(Result.ErrorType.RUNTIME, 
                "Error converting list to string: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<Value> asBoolean() {
        return Result.ok(new ABoolean(!elements.isEmpty()));
    }

    @Override
    public String type() {
        return "list";
    }

    @Override
    public String toString() {
        Result<Value> stringResult = asString();
        if (stringResult.isError()) {
            return "[Error: " + stringResult.getErrorMessage() + "]";
        }
        return stringResult.getValue().toString();
    }

    public Value get(int i) {
        if (i >= 0 && i < elements.size()) {
            return elements.get(i);
        }
        return new ANull();
    }

    public void put(T val) {
        if (val == null || val instanceof ANull) {} 
        else if (elementType == null) {
            elementType = val.getClass(); 
        } else if (!elementType.equals(val.getClass())) {
            throw new IllegalArgumentException(
                "All elements must be of type: " + elementType.getSimpleName() +
                ", but got: " + val.getClass().getSimpleName()
            );
        }

        elements.add(val);
    }

    public Value set(int i, T v) {
        if (v != null && !(v instanceof ANull) && elementType != null && !elementType.equals(v.getClass())) {
            throw new IllegalArgumentException(
                "All elements must be of type: " + elementType.getSimpleName() +
                ", but got: " + v.getClass().getSimpleName()
            );
        }

        if (i >= 0 && i < elements.size()) {
            return elements.set(i, v);
        }

        return v;
    }

    public void clear() {
        elements = new ArrayList<>();
        elementType = null;
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public Object getValue() {
        return elements;
    }

    public Class<?> getElementType() {
        return elementType;
    }
}
