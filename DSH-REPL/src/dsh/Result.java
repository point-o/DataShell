package dsh;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Result type that throws no exceptions, while yielding the exception type
 * 
 * @param <T> The type of the success value
 * @author Ryan Pointer
 * @version 7/20/25
 */
public abstract class Result<T> {
    
    public static <T> Result<T> ok(T value) {
        return new Success<>(value);
    }
    
    public static <T> Result<T> error(ErrorType type, String message) {
        return new Failure<>(type, message, null);
    }
    
    // uses java throwables to our advantage
    public static <T> Result<T> error(ErrorType type, String message, Throwable cause) {
        return new Failure<>(type, message, cause);
    }
    
    public static <T> Result<T> error(String message) {
        return new Failure<>(ErrorType.RUNTIME, message, null);
    }
    
    public static <T> Result<T> error(String message, Throwable cause) {
        return new Failure<>(ErrorType.RUNTIME, message, cause);
    }
    
    public abstract boolean isOk(); // brilliant naming
    public abstract boolean isError();
    public abstract T getValue();
    public abstract String getErrorMessage();
    public abstract ErrorType getErrorType();
    public abstract Throwable getCause();
    
    // Convenience method for getting error message - useful for the Evaluator
    public String getError() {
        return getErrorMessage();
    }
    
    public T getValueOrDefault(T defaultValue) {
        return isOk() ? getValue() : defaultValue;
    }
    
    public T getValueOrElse(Supplier<T> supplier) {
        return isOk() ? getValue() : supplier.get();
    }
    
    // chain ops
    public <U> Result<U> map(Function<T, U> mapper) {
        if (isOk()) {
            try {
                return Result.ok(mapper.apply(getValue()));
            } catch (Exception e) {
                return Result.error(ErrorType.RUNTIME, "Error in map operation: " + e.getMessage(), e);
            }
        } else {
            return Result.error(getErrorType(), getErrorMessage(), getCause());
        }
    }
    
    // future me: mapping is for running next steps if the first step succeeds
    /*
     * // Convert string to number, then add 10
     *    Result<Value> result = convertToNumber("5")  // Ok(ANumber(5))
     *    (first step yields 5)
     *    .map(num -> num.add(new ANumber(10)));   // Ok(ANumber(15))
     *    (map extends to adding an additional number 10)
     */
    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        if (isOk()) {
            try {
                return mapper.apply(getValue());
            } catch (Exception e) {
                return Result.error(ErrorType.RUNTIME, "Error in flatMap operation: " + e.getMessage(), e);
            }
        } else {
            return Result.error(getErrorType(), getErrorMessage(), getCause());
        }
    }
    
    public Result<T> filter(Function<T, Boolean> predicate, String errorMessage) {
        if (isOk()) {
            try {
                if (predicate.apply(getValue())) {
                    return this;
                } else {
                    return Result.error(ErrorType.VALIDATION, errorMessage);
                }
            } catch (Exception e) {
                return Result.error(ErrorType.RUNTIME, "Error in filter predicate: " + e.getMessage(), e);
            }
        } else {
            return this;
        }
    }
    
    // Recovery operations
    public Result<T> recover(Function<Failure<T>, T> recovery) {
        if (isError()) {
            try {
                return Result.ok(recovery.apply((Failure<T>) this));
            } catch (Exception e) {
                return Result.error(ErrorType.RUNTIME, "Error in recovery function: " + e.getMessage(), e);
            }
        } else {
            return this;
        }
    }
    
    public Result<T> recoverWith(Function<Failure<T>, Result<T>> recovery) {
        if (isError()) {
            try {
                return recovery.apply((Failure<T>) this);
            } catch (Exception e) {
                return Result.error(ErrorType.RUNTIME, "Error in recovery function: " + e.getMessage(), e);
            }
        } else {
            return this;
        }
    }
    
    // Utility methods
    public void ifOk(java.util.function.Consumer<T> consumer) {
        if (isOk()) {
            consumer.accept(getValue());
        }
    }
    
    public void ifError(java.util.function.Consumer<Failure<T>> consumer) {
        if (isError()) {
            consumer.accept((Failure<T>) this);
        }
    }
    
    @Override
    public String toString() {
        if (isOk()) {
            return "Ok(" + getValue() + ")";
        } else {
            return "Error(" + getErrorType() + ": " + getErrorMessage() + ")";
        }
    }
    
    // Success implementation
    private static class Success<T> extends Result<T> {
        private final T value;
        
        private Success(T value) {
            this.value = value;
        }
        
        @Override
        public boolean isOk() { return true; }
        
        @Override
        public boolean isError() { return false; }
        
        @Override
        public T getValue() { return value; }
        
        @Override
        public String getErrorMessage() {
            throw new IllegalStateException("Cannot get error message from success result");
        }
        
        @Override
        public ErrorType getErrorType() {
            throw new IllegalStateException("Cannot get error type from success result");
        }
        
        @Override
        public Throwable getCause() {
            throw new IllegalStateException("Cannot get cause from success result");
        }
    }
    
    // Failure implementation
    public static class Failure<T> extends Result<T> {
        private final ErrorType errorType;
        private final String message;
        private final Throwable cause;
        
        private Failure(ErrorType errorType, String message, Throwable cause) {
            this.errorType = errorType;
            this.message = message;
            this.cause = cause;
        }
        
        @Override
        public boolean isOk() { return false; }
        
        @Override
        public boolean isError() { return true; }
        
        @Override
        public T getValue() {
            throw new IllegalStateException("Cannot get value from error result");
        }
        
        @Override
        public String getErrorMessage() { return message; }
        
        @Override
        public ErrorType getErrorType() { return errorType; }
        
        @Override
        public Throwable getCause() { return cause; }
    }
    
    // Error type enumeration
    public enum ErrorType {
        ARITHMETIC("Arithmetic error"),
        TYPE_CONVERSION("Type conversion error"), 
        VALIDATION("Validation error"),
        SYNTAX("Syntax error"),
        NULL_VALUE("Null value error"),
        RUNTIME("Runtime error"),
        EVALUATION("Evaluation error"),  // Added for Phase 2 Evaluator
        UNSUPPORTED_OPERATION("Unsupported operation"),
        INDEX_OUT_OF_BOUNDS("Index out of bounds"),
        INVALID_ARGUMENT("Invalid argument");
        
        private final String description;
        
        ErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
}