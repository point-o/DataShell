package dsh;

/**
 * A class to represent a String
 * 
 * @author Ryan Pointer
 * @version 7/9/25
 */
public class AString implements Value {
	
	private final String value;
	
	public AString(String value) {
		this.value = value;
	}
	
	public Result<Value> asNumber() {
		try {
			return Result.ok(new ANumber(new java.math.BigDecimal(value.trim())));
		} catch (NumberFormatException e){
			return Result.ok(new ANumber(java.math.BigDecimal.valueOf(Double.NaN)));
		}
	}

	public Result<Value> asString() {
		return Result.ok(this);
	}

	public Result<Value> asBoolean() {
		return Result.ok(new ABoolean(!value.isEmpty()));
	}

	public String type() {
		return "string";
	}
	
	public String toString() {
		return value;
	}

	@Override
	public Object getValue() {
		return value;
	}
}
