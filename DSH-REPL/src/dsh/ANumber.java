package dsh;

import java.math.BigDecimal;
/**
 * A class to represent a Number
 * Since this is a REPL, performance isn't too important.
 * So, these numbers will be represented as Java's BigDecimal
 * to guarantee precision, and for easier casting to Strings
 * 
 * @author Ryan Pointer
 * @version 7/9/25
 */
public class ANumber implements Value{
	
	private final BigDecimal value;
	
	public ANumber(BigDecimal value) {
		this.value = value;
	}
	
	public ANumber(Integer value) {
		this.value = new BigDecimal(value);
	}
	
	public ANumber(Double value) {
		this.value = new BigDecimal(value);
	}
	
	public BigDecimal getValue() {
		return value;
	}
	
	public Result<Value> asNumber() {
		return Result.ok(this);
	}

	public Result<Value> asString() {
		return Result.ok(new AString(value.stripTrailingZeros().toString()));
	}

	public Result<Value> asBoolean() {
		if (value.equals(java.math.BigDecimal.ZERO)) {
			return Result.ok(new ABoolean(false));
		}
		return Result.ok(new ABoolean(true));
	}

	public String type() {
		return "number";
	}
	
	public String toString() {
		return value.stripTrailingZeros().toString();
	}

}
