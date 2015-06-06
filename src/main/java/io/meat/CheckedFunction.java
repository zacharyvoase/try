package io.meat;

import java.util.function.Function;

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
