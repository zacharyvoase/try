package io.meat;

import java.util.function.Function;

/**
 * A {@link java.util.function.Function} which can throw any exception.
 *
 * <p>Use the {@link #toUnchecked()} method to get a standard Function that
 * wraps any checked exceptions thrown by the function in a
 * {@link RuntimeException} (if they're not already RuntimeExceptions).</p>
 *
 * @param <T> the input type of the function
 * @param <R> the output type of the function
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {
    R apply(T t) throws Exception;

    default Function<T, R> toUnchecked() {
        return (input) -> {
            try {
                return apply(input);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
