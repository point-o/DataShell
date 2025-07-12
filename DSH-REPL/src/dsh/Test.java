package dsh;

public class Test {

	public static void main(String[] args) {
		Environment env = new Environment();
		env.set("e", new AString("hey"));
		Calculator calc = new Calculator(env);
		System.out.println(calc.evaluate("((2*3)+4^1) + e"));
	}

}
