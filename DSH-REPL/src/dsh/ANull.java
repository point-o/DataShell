package dsh;

import java.math.BigDecimal;

/**
 * A class to represent a Null
 * 
 * @author Ryan Pointer
 * @version 7/9/25
 */
public class ANull implements Value{

	public ANull() {}
	
	public Result<Value> asNumber() {
		return Result.ok(new ANumber(new BigDecimal(0)));
	}

	public Result<Value> asString() {
		return Result.ok(new AString("null"));
	}

	public Result<Value> asBoolean() {
		return Result.ok(new ABoolean(false));
	}

	public String type() {
		return "null";
	}
	
	public String toString() {
		return "null";
	}

	@Override
	public Object getValue() {
		return "null";
	}
	
}
