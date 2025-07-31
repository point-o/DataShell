package dsh;
import java.util.*;
import java.io.IOException;
import java.math.BigInteger;

/**
 * A class to hold commands
 *
 * @author Ryan Pointer
 * @version 7/16/25
 */
public class CommandRegistry {
    private Map<String, Command> commands;
    
    public CommandRegistry() {
        this.commands = new HashMap<>();
        initializeCommands();
    }
    
    public int size() {
        return commands.size();
    }
    
    private void initializeCommands() {
        // Matrix command example
        registerCommand("size", new Command(
            "Show matrix dimensions",
            "size <matrix>",
            this::matrixSize,
            1
        ));
        
        // Discrete Math Commands
        registerCommand("factorial", new Command(
            "Calculate factorial of a number",
            "factorial <n>",
            this::factorial,
            1
        ));
        
        registerCommand("permutation", new Command(
            "Calculate permutation P(n,r) = n!/(n-r)!",
            "permutation <n> <r>",
            this::permutation,
            2
        ));
        
        registerCommand("combination", new Command(
            "Calculate combination C(n,r) = n!/(r!(n-r)!)",
            "combination <n> <r>",
            this::combination,
            2
        ));
        
        registerCommand("gcd", new Command(
            "Calculate greatest common divisor",
            "gcd <a> <b>",
            this::gcd,
            2
        ));
        
        registerCommand("lcm", new Command(
            "Calculate least common multiple",
            "lcm <a> <b>",
            this::lcm,
            2
        ));
        
        registerCommand("modpow", new Command(
            "Calculate modular exponentiation (a^b mod m)",
            "modpow <base> <exponent> <modulus>",
            this::modularPower,
            3
        ));
        
        registerCommand("isprime", new Command(
            "Check if a number is prime",
            "isprime <n>",
            this::isPrime,
            1
        ));
        
        registerCommand("fibonacci", new Command(
            "Generate Fibonacci sequence up to n terms",
            "fibonacci <n>",
            this::fibonacci,
            1
        ));
        
        registerCommand("primes", new Command(
            "Generate prime numbers up to n",
            "primes <n>",
            this::primesUpTo,
            1
        ));
        
        registerCommand("binomial", new Command(
            "Calculate binomial coefficient (same as combination)",
            "binomial <n> <k>",
            this::combination,
            2
        ));
        
        registerCommand("derangements", new Command(
            "Calculate number of derangements of n objects",
            "derangements <n>",
            this::derangements,
            1
        ));
        
        registerCommand("catalan", new Command(
            "Calculate nth Catalan number",
            "catalan <n>",
            this::catalan,
            1
        ));
    }
    
    // Discrete Math Command Implementations
    
    private Value factorial(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide a number");
        }
        
        Result<Value> numberResult = args[0].asNumber();
        if (numberResult.isError()) {
            return new AString("Error: Invalid number format");
        }
        
        int n = ((ANumber) numberResult.getValue()).getValue().intValue();
        if (n < 0) {
            return new AString("Error: Factorial undefined for negative numbers");
        }
        if (n > 20) {
            return new AString("Error: Result too large - use values ≤ 20");
        }
        
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        
        return new AString(n + "! = " + result.toString());
    }
    
    private Value permutation(Environment context, Value... args) {
        if (args.length < 2) {
            return new AString("Error: Provide two numbers (n, r)");
        }
        
        Result<Value> nResult = args[0].asNumber();
        Result<Value> rResult = args[1].asNumber();
        
        if (nResult.isError() || rResult.isError()) {
            return new AString("Error: Invalid number format");
        }
        
        int n = ((ANumber) nResult.getValue()).getValue().intValue();
        int r = ((ANumber) rResult.getValue()).getValue().intValue();
        
        if (n < 0 || r < 0) {
            return new AString("Error: Values must be non-negative");
        }
        if (r > n) {
            return new AString("Error: r cannot be greater than n");
        }
        
        BigInteger result = BigInteger.ONE;
        for (int i = n; i > n - r; i--) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        
        return new AString("P(" + n + "," + r + ") = " + result.toString());
    }
    
    private Value combination(Environment context, Value... args) {
        if (args.length < 2) {
            return new AString("Error: Provide two numbers (n, r)");
        }
        
        Result<Value> nResult = args[0].asNumber();
        Result<Value> rResult = args[1].asNumber();
        
        if (nResult.isError() || rResult.isError()) {
            return new AString("Error: Invalid number format");
        }
        
        int n = ((ANumber) nResult.getValue()).getValue().intValue();
        int r = ((ANumber) rResult.getValue()).getValue().intValue();
        
        if (n < 0 || r < 0) {
            return new AString("Error: Values must be non-negative");
        }
        if (r > n) {
            return new AString("Error: r cannot be greater than n");
        }
        
        // Use the more efficient formula: C(n,r) = C(n, n-r)
        int originalR = r;
        r = Math.min(r, n - r);
        
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < r; i++) {
            result = result.multiply(BigInteger.valueOf(n - i));
            result = result.divide(BigInteger.valueOf(i + 1));
        }
        
        return new AString("C(" + n + "," + originalR + ") = " + result.toString());
    }
    
    private Value gcd(Environment context, Value... args) {
        if (args.length < 2) {
            return new AString("Error: Provide two numbers");
        }
        
        Result<Value> aResult = args[0].asNumber();
        Result<Value> bResult = args[1].asNumber();
        
        if (aResult.isError() || bResult.isError()) {
            return new AString("Error: Invalid number format");
        }
        
        long a = ((ANumber) aResult.getValue()).getValue().longValue();
        long b = ((ANumber) bResult.getValue()).getValue().longValue();
        
        long result = gcdHelper(Math.abs(a), Math.abs(b));
        return new AString("gcd(" + a + ", " + b + ") = " + result);
    }
    
    private long gcdHelper(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    
    private Value lcm(Environment context, Value... args) {
        if (args.length < 2) {
            return new AString("Error: Provide two numbers");
        }
        
        Result<Value> aResult = args[0].asNumber();
        Result<Value> bResult = args[1].asNumber();
        
        if (aResult.isError() || bResult.isError()) {
            return new AString("Error: Invalid number format");
        }
        
        long a = ((ANumber) aResult.getValue()).getValue().longValue();
        long b = ((ANumber) bResult.getValue()).getValue().longValue();
        
        if (a == 0 || b == 0) {
            return new AString("Error: LCM undefined for zero");
        }
        
        long gcd = gcdHelper(Math.abs(a), Math.abs(b));
        long result = Math.abs(a * b) / gcd;
        return new AString("lcm(" + a + ", " + b + ") = " + result);
    }
    
    private Value modularPower(Environment context, Value... args) {
        if (args.length < 3) {
            return new AString("Error: Provide three numbers (base, exponent, modulus)");
        }
        
        Result<Value> baseResult = args[0].asNumber();
        Result<Value> expResult = args[1].asNumber();
        Result<Value> modResult = args[2].asNumber();
        
        if (baseResult.isError() || expResult.isError() || modResult.isError()) {
            return new AString("Error: Invalid number format");
        }
        
        long base = ((ANumber) baseResult.getValue()).getValue().longValue();
        long exponent = ((ANumber) expResult.getValue()).getValue().longValue();
        long modulus = ((ANumber) modResult.getValue()).getValue().longValue();
        
        if (modulus == 0) {
            return new AString("Error: Modulus cannot be zero");
        }
        if (exponent < 0) {
            return new AString("Error: Negative exponents not supported");
        }
        
        BigInteger bigBase = BigInteger.valueOf(base);
        BigInteger bigExp = BigInteger.valueOf(exponent);
        BigInteger bigMod = BigInteger.valueOf(modulus);
        
        BigInteger result = bigBase.modPow(bigExp, bigMod);
        return new AString(base + "^" + exponent + " mod " + modulus + " = " + result);
    }
    
    private Value isPrime(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide a number");
        }
        
        Result<Value> numberResult = args[0].asNumber();
        if (numberResult.isError()) {
            return new AString("Error: Invalid number format");
        }
        
        long n = ((ANumber) numberResult.getValue()).getValue().longValue();
        if (n < 2) {
            return new ABoolean(false);
        }
        if (n == 2) {
            return new ABoolean(true);
        }
        if (n % 2 == 0) {
            return new ABoolean(false);
        }
        
        for (long i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return new ABoolean(false);
            }
        }
        return new ABoolean(true);
    }
    
    private Value fibonacci(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide number of terms");
        }
        
        Result<Value> numberResult = args[0].asNumber();
        if (numberResult.isError()) {
            return new AString("Error: Invalid number format");
        }
        
        int n = ((ANumber) numberResult.getValue()).getValue().intValue();
        if (n <= 0) {
            return new AString("Error: Number of terms must be positive");
        }
        if (n > 50) {
            return new AString("Error: Too many terms - use values ≤ 50");
        }
        
        List<BigInteger> fib = new ArrayList<>();
        if (n >= 1) fib.add(BigInteger.ZERO);
        if (n >= 2) fib.add(BigInteger.ONE);
        
        for (int i = 2; i < n; i++) {
            fib.add(fib.get(i-1).add(fib.get(i-2)));
        }
        
        StringBuilder result = new StringBuilder("Fibonacci(" + n + "): ");
        for (int i = 0; i < fib.size(); i++) {
            result.append(fib.get(i));
            if (i < fib.size() - 1) result.append(", ");
        }
        
        return new AString(result.toString());
    }
    
    private Value primesUpTo(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide upper limit");
        }
        
        Result<Value> numberResult = args[0].asNumber();
        if (numberResult.isError()) {
            return new AString("Error: Invalid number format");
        }
        
        int n = ((ANumber) numberResult.getValue()).getValue().intValue();
        if (n < 2) {
            return new AString("No primes less than 2");
        }
        if (n > 1000) {
            return new AString("Error: Too large - use values ≤ 1000");
        }
        
        List<Integer> primes = sieveOfEratosthenes(n);
        StringBuilder result = new StringBuilder("Primes up to " + n + ": ");
        for (int i = 0; i < primes.size(); i++) {
            result.append(primes.get(i));
            if (i < primes.size() - 1) result.append(", ");
        }
        result.append(" (").append(primes.size()).append(" primes)");
        
        return new AString(result.toString());
    }
    
    private List<Integer> sieveOfEratosthenes(int n) {
        boolean[] isPrime = new boolean[n + 1];
        for (int i = 2; i <= n; i++) isPrime[i] = true;
        
        for (int i = 2; i * i <= n; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j <= n; j += i) {
                    isPrime[j] = false;
                }
            }
        }
        
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            if (isPrime[i]) primes.add(i);
        }
        return primes;
    }
    
    private Value derangements(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide a number");
        }
        
        Result<Value> numberResult = args[0].asNumber();
        if (numberResult.isError()) {
            return new AString("Error: Invalid number format");
        }
        
        int n = ((ANumber) numberResult.getValue()).getValue().intValue();
        if (n < 0) {
            return new AString("Error: n must be non-negative");
        }
        if (n > 20) {
            return new AString("Error: Too large - use values ≤ 20");
        }
        
        if (n == 0) return new AString("D(0) = 1");
        if (n == 1) return new AString("D(1) = 0");
        
        BigInteger[] d = new BigInteger[n + 1];
        d[0] = BigInteger.ONE;
        d[1] = BigInteger.ZERO;
        
        for (int i = 2; i <= n; i++) {
            d[i] = BigInteger.valueOf(i - 1).multiply(d[i - 1].add(d[i - 2]));
        }
        
        return new AString("D(" + n + ") = " + d[n]);
    }
    
    private Value catalan(Environment context, Value... args) {
        if (args.length == 0) {
            return new AString("Error: Provide a number");
        }
        
        Result<Value> numberResult = args[0].asNumber();
        if (numberResult.isError()) {
            return new AString("Error: Invalid number format");
        }
        
        int n = ((ANumber) numberResult.getValue()).getValue().intValue();
        if (n < 0) {
            return new AString("Error: n must be non-negative");
        }
        if (n > 30) {
            return new AString("Error: Too large - use values ≤ 30");
        }
        
        // C_n = (2n)! / ((n+1)! * n!)
        BigInteger numerator = BigInteger.ONE;
        BigInteger denominator = BigInteger.ONE;
        
        for (int i = n + 2; i <= 2 * n; i++) {
            numerator = numerator.multiply(BigInteger.valueOf(i));
        }
        
        for (int i = 2; i <= n; i++) {
            denominator = denominator.multiply(BigInteger.valueOf(i));
        }
        
        BigInteger result = numerator.divide(denominator);
        return new AString("C(" + n + ") = " + result);
    }
    
    // Placeholder for matrix size method
    private Value matrixSize(Environment context, Value... args) {
        // Implementation depends on your matrix structure
        return new AString("Matrix size functionality not implemented");
    }
    
    // Registry management methods
    public void registerCommand(String name, Command command) {
        commands.put(name, command);
    }
    
    public Command getCommand(String name) {
        return commands.get(name);
    }
    
    public boolean hasCommand(String name) {
        return commands.containsKey(name);
    }
    
    public java.util.Set<String> getCommandNames() {
        return commands.keySet();
    }
    
    public java.util.Map<String, Command> getAllCommands() {
        return new java.util.HashMap<>(commands);
    }
    
    public boolean exists(String commandName) {
        return commands.get(commandName) != null;
    }
}