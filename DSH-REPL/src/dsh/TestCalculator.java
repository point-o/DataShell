package dsh;

public class TestCalculator {

    public static void main(String[] args) {
        Environment env = new Environment();
        Calculator calc = new Calculator(env);

        System.out.println("== Basic Literals ==");
        test(calc, "42");
        test(calc, "3.14");
        test(calc, "\"hello world\"");
        test(calc, "true");
        test(calc, "false");
        test(calc, "null");

        System.out.println("\n== Arrays ==");
        test(calc, "[1, 2, 3]");
        test(calc, "[42]");
        test(calc, "[1, \"hi\", true]");

        System.out.println("\n== Arithmetic ==");
        test(calc, "1 + 2");
        test(calc, "5 - 3");
        test(calc, "2 * 4");
        test(calc, "10 / 2");
        test(calc, "9 % 4");
        test(calc, "2 ^ 3");

        System.out.println("\n== Precedence & Grouping ==");
        test(calc, "2 + 3 * 4");
        test(calc, "(2 + 3) * 4");
        test(calc, "(((((7)))))");
        test(calc, "(8 - 2) * (3 + 1)");

        System.out.println("\n== Variables ==");
        env.set("x", new ANumber(new java.math.BigDecimal("100")));
        env.set("msg", new AString("yo"));
        test(calc, "x");
        test(calc, "x + 50");
        test(calc, "msg");
        test(calc, "msg + \" there\"");

        System.out.println("\n== Error Handling ==");
        test(calc, "unclosed(");
        test(calc, "unknownVar");
        test(calc, "1 / 0");
        test(calc, "[] + 1");
    }

    private static void test(Calculator calc, String expr) {
        Result<Value> result = calc.evaluate(expr);
        if (result.isError()) {
            System.out.println("❌ " + expr + " => ERROR: " + result.getErrorMessage());
        } else {
            System.out.println("✅ " + expr + " => " + result.getValue());
        }
    }
}
