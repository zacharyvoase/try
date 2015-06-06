package io.meat;

import java.util.function.Supplier;

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
