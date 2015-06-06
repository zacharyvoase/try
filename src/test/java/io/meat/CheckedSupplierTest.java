package io.meat;

import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CheckedSupplierTest {

    @Test
    public void testToUncheckedWrapsCheckedExceptions() throws Exception {
        CheckedSupplier<Integer> func = () -> {
            throw new IOException("Hello");
        };
        Supplier<Integer> unchecked = func.toUnchecked();
        try {
            unchecked.get();
            fail("Calling the Supplier should have raised an exception");
        } catch (RuntimeException e) {
            assertEquals(
                    "The cause of the RuntimeException should be the unchecked exception",
                    e.getCause().getClass(),
                    IOException.class);
        }
    }

    @Test
    public void testToUncheckedDoesntWrapUncheckedExceptions() throws Exception {
        CheckedSupplier<Integer> func = () -> {
            throw new ArithmeticException("Hello");
        };
        Supplier<Integer> unchecked = func.toUnchecked();
        try {
            unchecked.get();
            fail("Calling the Supplier should have raised an exception");
        } catch (RuntimeException e) {
            assertEquals(
                    "The exception should not be wrapped in another RuntimeException",
                    e.getClass(),
                    ArithmeticException.class);
        }
    }
}

