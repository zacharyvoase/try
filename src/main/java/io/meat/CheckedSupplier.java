package io.meat;

import java.util.function.Supplier;

/**
 * A {@link java.util.function.Supplier} which can throw any exception.
 *
 * <p>Use the {@link #toUnchecked()} method to get a standard Supplier that
 * wraps any checked exceptions thrown by {@link #get()} in a
 * {@link RuntimeException} (if they're not already RuntimeExceptions).</p>
 *
 * @param <T> the type of value produced by this Supplier
 */
@FunctionalInterface
public interface CheckedSupplier<T> {
    T get() throws Exception;

    default Supplier<T> toUnchecked() {
        return () -> {
            try {
                return get();
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
