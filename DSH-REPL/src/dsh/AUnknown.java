package dsh;

/**
 * A class to represent an Unknown (i dont think ill use this but who knows)
 * 
 * @author Ryan Pointer
 * @version 7/9/25
 */
public class AUnknown implements Value{

	@Override
	public Result<Value> asNumber() {
		return Result.ok(new ANumber(java.math.BigDecimal.ZERO));
	}

	@Override
	public Result<Value> asString() {
		return Result.ok(new AString("unknown"));
	}

	@Override
	public Result<Value> asBoolean() {
		return Result.ok(new ABoolean(false));
	}

	@Override
	public String type() {
		return "unknown";
	}
	
	public String toString() {
		return "unknown";
	}

	public Object getValue() {
		return "unknown";
	}
}
