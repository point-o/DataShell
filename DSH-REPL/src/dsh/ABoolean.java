package dsh;

/**
 * A class to represent a Boolean
 * 
 * @author Ryan Pointer
 * @version 7/9/25
 */
public class ABoolean implements Value{
	private final Boolean value;
	
	public ABoolean(Boolean value) {
		this.value = value;
	}
	
	public Boolean getValue() {
		return value;
	}
	
	public Result<Value> asNumber() {
		if (value) {
			return Result.ok(new ANumber(java.math.BigDecimal.ONE));
		}
		return Result.ok(new ANumber(java.math.BigDecimal.ZERO));
	}

	public Result<Value> asString() {
		return Result.ok(new AString(Boolean.toString(value)));
	}

	public Result<Value> asBoolean() {
		return Result.ok(this);
	}

	public String type() {
		return "boolean";
	}
	
	public String toString() {
		return value.toString();
	}
}
