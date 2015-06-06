package io.meat;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TryTest {
    @Test(expected = IllegalArgumentException.class)
    public void testCannotSucceedWithNull() {
        Try.succeed(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotFailWithNull() {
        Try.fail(null);
    }

    @Test
    public void testResultAndFailureForSuccess() {
        Try<Integer> successful = Try.succeed(123);
        assertTrue(
                "getResult() of a successful Try should be a non-empty Optional",
                successful.getResult().isPresent());
        assertEquals(successful.getResult().get(), (Integer) 123);
        assertFalse(
                "getFailure() of a successful Try should be an empty Optional",
                successful.getFailure().isPresent());
    }

    @Test
    public void testResultAndFailureForFailure() {
        Exception error = new IllegalStateException("Something went wrong");
        Try<Integer> failed = Try.fail(error);
        assertFalse(
                "getResult() of a failed Try should be an empty Optional",
                failed.getResult().isPresent());
        assertTrue(
                "getFailure() of a failed Try should be a non-empty Optional",
                failed.getFailure().isPresent());
        assertEquals(failed.getFailure().get(), error);
    }

    @Test
    public void testAttemptReturnsSuccess() {
        Try<Integer> successful = Try.attempt(() -> 123);
        assertEquals(
                "Try.attempt() should return a successful Try for a successful attempt",
                Try.succeed(123), successful);
    }

    @Test
    public void testAttemptCapturesRuntimeExceptions() {
        IllegalStateException error = new IllegalStateException("Things broke");
        Try<Integer> failed = Try.attempt(() -> {
            if (true) {
                throw error;
            } else {
                // this will never happen
                return 123;
            }
        });
        assertEquals(
                "Try.attempt() should return a failed Try for an uncaught exception",
                Try.fail(error), failed);
    }

    @Test
    public void testAttemptCheckedReturnsSuccess() {
        Try<Integer> successful = Try.attemptChecked(() -> 123);
        assertEquals(
                "Try.attemptChecked() should return a successful Try for a successful attempt",
                Try.succeed(123), successful);
    }

    @Test
    public void testAttemptCheckedCapturesCheckedExceptions() {
        IOException error = new IOException("Things broke");
        Try<Integer> failed = Try.attemptChecked(() -> {
            if (true) {
                throw error;
            } else {
                // this will never happen
                return 123;
            }
        });
        assertEquals(
                "Try.attemptChecked() should return a failed Try for a checked exception",
                Try.fail(error), failed);
    }

    @Test
    public void testAttemptApplyReturnsSuccess() {
        Try<String> successful = Try.attemptApply(Integer::toHexString, 123);
        assertEquals(
                "Try.attemptApply() should return a successful Try for a successful application",
                Try.succeed("7b"), successful);
    }

    @Test
    public void testAttemptApplyCapturesRuntimeExceptions() {
        Try<Integer> failed = Try.attemptApply(number -> number / 0, 123);
        assertEquals(
                "Try.attemptApply() should return a failed Try for an uncaught exception",
                ArithmeticException.class,
                failed.getFailure().get().getClass());
    }

    @Test
    public void testAttemptApplyCheckedReturnsSuccess() {
        Try<String> successful = Try.attemptApplyChecked(Integer::toHexString, 123);
        assertEquals(
                "Try.attemptApplyChecked() should return a successful Try for a successful application",
                Try.succeed("7b"), successful);
    }

    @Test
    public void testAttemptApplyCheckedCapturesCheckedExceptions() {
        IOException error = new IOException("Things broke");
        Try<String> failed = Try.attemptApplyChecked(number -> {
            if (true) {
                throw error;
            } else {
                return "this will never happen";
            }
        }, 123);
        assertEquals(
                "Try.attemptApplyChecked() should return a failed Try for a checked exception",
                Try.fail(error), failed);
    }

    @Test
    public void testGetShouldReturnResultForSuccess() {
        String value = "A successful value";
        assertEquals(
                "Try.get() should return the result for a successful Try",
                value,
                Try.succeed(value).get());
    }

    @Test
    public void testGetShouldThrowAnUncheckedExceptionForFailure() {
        ArithmeticException error = new ArithmeticException("Probably divided by zero");
        try {
            Try.fail(error).get();
            fail("Try.get() should throw an unchecked exception for a failed Try");
        } catch (Exception e) {
            assertEquals(
                    "Try.get() should throw a RuntimeException",
                    RuntimeException.class,
                    e.getClass());
            assertEquals(
                    "Try.get() should throw a RuntimeException caused by the failure of the Try",
                    error,
                    e.getCause());
        }
    }

    @Test
    public void testTrySuccessString() {
        assertEquals("Try{result=123}", Try.succeed(123).toString());
    }

    @Test
    public void testTryFailureString() {
        Exception error = new IllegalStateException("Some error");
        assertEquals(
                "Try{failure=java.lang.IllegalStateException: Some error}",
                Try.fail(error).toString());
    }

    @Test
    public void testTryHashCode() {
        Try<Integer> result1 = Try.succeed(123);
        Try<String> result2 = Try.succeed("Hello");
        Try<String> result3 = Try.succeed("Hello");
        Try<String> result4 = Try.succeed("World");
        Exception error1 = new ArithmeticException("Things got divided by zero");
        Exception error2 = new IllegalStateException("Things broke");
        Try<Integer> failure1 = Try.fail(error1);
        Try<Integer> failure2 = Try.fail(error2);
        Try<String> failure3 = Try.fail(error1);
        Try<Integer> failure4 = Try.fail(error1);

        assertEquals("Identical successful Trys should have the same hashCode",
                result2.hashCode(), result2.hashCode());
        assertEquals("Same type and equal but distinct successful Trys should have the same hashCode",
                result2.hashCode(), result3.hashCode());
        assertNotEquals("Same type but unequal successful Trys should not have the same hashCode",
                result3.hashCode(), result4.hashCode());
        assertNotEquals("Differently-typed successful Trys should not have the same hashCode",
                result1.hashCode(), result2.hashCode());

        assertEquals("Same type failed Trys with equal exceptions should have the same hashCode",
                failure1.hashCode(), failure4.hashCode());
        assertEquals("Differently-typed failed Trys with equal exceptions should have the same hashCode",
                failure1.hashCode(), failure3.hashCode());
        assertNotEquals("Same-typed failed Trys with different exceptions should not have the same hashCode",
                failure1.hashCode(), failure2.hashCode());
        assertNotEquals("Differently-typed failed Trys with different exceptions should not have the same hashCode",
                failure2.hashCode(), failure3.hashCode());
    }

    @Test
    public void testTryEquality() {
        Try<Integer> result1 = Try.succeed(123);
        Try<String> result2 = Try.succeed("Hello");
        Try<String> result3 = Try.succeed("Hello");
        Try<String> result4 = Try.succeed("World");
        Exception error1 = new ArithmeticException("Things got divided by zero");
        Exception error2 = new IllegalStateException("Things broke");
        Try<Integer> failure1 = Try.fail(error1);
        Try<Integer> failure2 = Try.fail(error2);
        Try<String> failure3 = Try.fail(error1);
        Try<Integer> failure4 = Try.fail(error1);

        assertEquals("Identical successful Trys should be equal",
                result2, result2);
        assertEquals("Same type and equal but distinct successful Trys should be equal",
                result2, result3);
        assertNotEquals("Same type but unequal successful Trys should not be equal",
                result3, result4);
        assertNotEquals("Differently-typed successful Trys should not be equal",
                result1, result2);

        assertEquals("Same type failed Trys with equal exceptions should be equal",
                failure1, failure4);
        assertEquals("Differently-typed failed Trys with equal exceptions should be equal",
                failure1, failure3);
        assertNotEquals("Same-typed failed Trys with different exceptions should not be equal",
                failure1, failure2);
        assertNotEquals("Differently-typed failed Trys with different exceptions should not be equal",
                failure2, failure3);
    }

    @Test
    public void testMapOnSuccessReturnsNewResult() {
        Try<Integer> successful = Try.succeed(123);
        Try<String> mapped = successful.map(Integer::toHexString);
        assertEquals(
                "map() on a successful Try should return the transformed result",
                Try.succeed("7b"), mapped);
    }

    @Test
    public void testMapOnSuccessWhichThrowsReturnsNewFailure() {
        ArithmeticException error = new ArithmeticException("Pretend we had a divide by zero");
        Try<Integer> successful = Try.succeed(123);
        Try<String> mapped = successful.map((number) -> {
            if (true) {
                throw error;
            } else {
                return "this will never happen";
            }
        });
        assertEquals(
                "If the mapping function fails, the result should be a failure of the exception it threw",
                Try.fail(error), mapped);
    }

    @Test
    public void testMapOnFailureReturnsOldFailure() {
        Exception error = new IllegalStateException("Something went wrong");
        Try<Integer> failed = Try.fail(error);
        Try<String> mapped = failed.map((number) -> {
            throw new ArithmeticException(
                    "The map function should never be called for a failed Try");
        });
        assertEquals(
                "Mapping a failed Try should produce the same failed Try",
                failed, mapped);
    }

    @Test
    public void testFlatMapOnSuccessReturningSuccessReturnsNewResult() {
        Try<Integer> successful = Try.succeed(123);
        Try<String> flatMapped = successful
                .flatMap((number) -> Try.succeed(Integer.toHexString(number)));
        assertEquals(
                "flatMapping a successful Try should produce a new Try of the result",
                Try.succeed("7b"), flatMapped);
    }

    @Test
    public void testFlatMapOnSuccessReturningFailureReturnsNewFailure() {
        Exception error = new ArithmeticException("Someone tried to divide by zero");
        Try<Integer> successful = Try.succeed(123);
        Try<String> flatMapped = successful
                .flatMap((number) -> Try.fail(error));
        assertEquals(
                "flatMapping to a failure should produce that failure",
                Try.fail(error), flatMapped);
    }

    @Test
    public void testFlatMapOnSuccessWhichThrowsReturnsNewFailure() {
        IllegalStateException error = new IllegalStateException("Everything broke");
        Try<Integer> successful = Try.succeed(123);
        Try<String> flatMapped = successful
                .flatMap((number) -> {
                    if (true) {
                        throw error;
                    } else {
                        return Try.succeed("this will never happen");
                    }
                });
        assertEquals(
                "If the flatMapping function fails, the result should be a failure of the exception it threw",
                Try.fail(error), flatMapped);
    }
}
