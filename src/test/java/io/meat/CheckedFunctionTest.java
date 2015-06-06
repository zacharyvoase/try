package io.meat;

import org.junit.Test;

import java.io.IOException;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CheckedFunctionTest {

    @Test
    public void testToUncheckedWrapsCheckedExceptions() throws Exception {
        CheckedFunction<Integer, String> func = integer -> {
            throw new IOException("Hello");
        };
        Function<Integer, String> unchecked = func.toUnchecked();
        try {
            unchecked.apply(123);
            fail("Applying the function should have raised an exception");
        } catch (RuntimeException e) {
            assertEquals(
                    "The cause of the RuntimeException should be the unchecked exception",
                    e.getCause().getClass(),
                    IOException.class);
        }
    }

    @Test
    public void testToUncheckedDoesntWrapUncheckedExceptions() throws Exception {
        CheckedFunction<Integer, String> func = integer -> {
            throw new ArithmeticException("Hello");
        };
        Function<Integer, String> unchecked = func.toUnchecked();
        try {
            unchecked.apply(123);
            fail("Applying the function should have raised an exception");
        } catch (RuntimeException e) {
            assertEquals(
                    "The exception should not be wrapped in another RuntimeException",
                    e.getClass(),
                    ArithmeticException.class);
        }
    }
}
