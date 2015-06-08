package io.meat;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A completed attempt to compute a result that either succeeded or failed.
 *
 * <p>Try is especially useful for monadic composition of tasks that may either
 * succeed or fail, without having to use a series of nested try/catch
 * statements. You can form a Try using {@link #attempt(Supplier)} or
 * {@link #attemptApply(Function, Object)}:</p>
 *
 * <pre>{@code
 * Try<String> value = Try.attempt(() -> keyValueStore.fetch("someKey"));
 * Try<String> value = Try.attemptApply(keyValueStore::fetch, "someKey");
 * }</pre>
 *
 * <p>If the function has checked exceptions, you can use
 * {@link #attemptChecked(CheckedSupplier)} or
 * {@link #attemptApplyChecked(CheckedFunction, Object)} to wrap any exception
 * in a RuntimeException.</p>
 *
 * <p>You can also manually make a successful or failed Try using
 * {@link #succeed(Object)} and {@link #fail(Throwable)}:</p>
 *
 * <pre>{@code
 * Try<String> value = Try.succeed("someValue");
 * Try<String> failure = Try.fail(new IllegalStateException("Something broke"));
 * }</pre>
 *
 * <p>Once you have a Try, you can transform the value inside it using
 * {@link #map(Function)}:</p>
 *
 * <pre>{@code
 * Try<String> hex = Try.succeed(123).map(Integer::toHexString);
 * assert hex.equals(Try.succeed("7b"));
 * }</pre>
 *
 * <p>If your function returns a Try, you can use {@link #flatMap(Function)} to
 * prevent nesting:</p>
 *
 * <pre>{@code
 * Try<Integer> number = Try.succeed(123);
 * Try<String> nextValue = number.flatMap(num -> {
 *     return Try.succeed("a string");
 * });
 * assert nextValue.equals(Try.succeed("a string"));
 * }</pre>
 *
 * <p>To get values back out of a Try, there are Optionals available as
 * {@link #getResult()} and {@link #getFailure()} for safe retrieval of either
 * state:</p>
 *
 * <pre>{@code
 * Try<Integer> number = Try.succeed(123);
 * assert number.getResult().equals(Optional.of(123));
 * assert !number.getFailure().isPresent();
 * }</pre>
 *
 * <p>If you've checked that a Try is successful, or you don't mind an (unchecked)
 * exception being raised, use {@link #get()}:</p>
 *
 * <pre>{@code
 * Try<Integer> number = Try.succeed(123);
 * assert number.get() == 123;
 * }</pre>
 *
 * <p>In the case of a failed Try, <tt>get()</tt> will wrap the exception in a
 * <tt>RuntimeException</tt>; its {@link Throwable#getCause()} will be the
 * original exception.</p>
 *
 * @param <Result> the type of result, if successful.
 */
public final class Try<Result> {

    private final Result result;
    private final Throwable failure;

    private Try(Result result, Throwable failure) {
        assert (result == null) ^ (failure == null)
                : "Exactly one of failure or result must be null";
        this.result = result;
        this.failure = failure;
    }

    /**
     * Build a successful Try from a non-null result.
     */
    public static <Result> Try<Result> succeed(Result result) {
        if (result == null) {
            throw new IllegalArgumentException("Try.succeed result may not be null");
        }
        return new Try<>(result, null);
    }

    /**
     * Build a failed Try from an exception or other Throwable.
     */
    public static <Result> Try<Result> fail(Throwable failure) {
        if (failure == null) {
            throw new IllegalArgumentException("Try.fail failure may not be null");
        }
        return new Try<>(null, failure);
    }

    /**
     * Wrap the result of a {@link Supplier} as a Try, catching exceptions.
     *
     * @param func a Supplier which returns a Result
     * @param <Result> the type of result returned by func
     * @return a Try containing either the Supplier's result, or any exception
     *         thrown by {@link Supplier#get()}
     */
    public static <Result> Try<Result> attempt(Supplier<Result> func) {
        return attemptChecked(func::get);
    }

    /**
     * Similar to {@link #attempt(Supplier)}, but handles checked exceptions.
     * @param func a CheckedSupplier which returns a Result or throws Exception
     * @param <Result> the type of result returned by func
     * @return a Try containing either the CheckedSupplier's result, or any
     *         exception thrown by {@link CheckedSupplier#get()}
     */
    public static <Result> Try<Result> attemptChecked(CheckedSupplier<Result> func) {
        Result result;
        try {
            result = func.get();
        } catch (Exception e) {
            return Try.fail(e);
        }
        return Try.succeed(result);
    }


    /**
     * Wrap the result of a {@link Function} as a Try, catching exceptions.
     *
     * @param func a Function from the input to the result type of the Try
     * @param input a single argument for func
     * @param <Input> the input type of the function
     * @param <Result> the result type of the function
     * @return a Try of type Result
     */
    public static <Input, Result> Try<Result> attemptApply(
            Function<? super Input, ? extends Result> func,
            Input input) {
        return attemptApplyChecked(func::apply, input);
    }

    /**
     * Similar to {@link #attemptApply(Function, Object)}, handling checked exceptions.
     *
     * @param func a Function from the input to the result type of the Try
     * @param input a single argument for func
     * @param <Input> the input type of the function
     * @param <Result> the result type of the function
     * @return a Try of type Result
     */
    public static <Input, Result> Try<Result> attemptApplyChecked(
            CheckedFunction<? super Input, ? extends Result> func,
            Input input) {
        Result result;
        try {
            result = func.apply(input);
        } catch (Exception e) {
            return Try.fail(e);
        }
        return Try.succeed(result);
    }

    public static <Result> CompletableFuture<Try<Result>> wrapFuture(CompletableFuture<Result> future) {
        return future.handle(Try::new);
    }

    /**
     * Get the successful result of this Try, or {@link Optional#empty()} if it failed.
     *
     * @return an Optional result
     */
    public Optional<Result> getResult() {
        return Optional.ofNullable(result);
    }

    /**
     * Get the Throwable that caused this Try to fail, or {@link Optional#empty()} if it was successful.
     *
     * @return an Optional Throwable
     */
    public Optional<Throwable> getFailure() {
        return Optional.ofNullable(failure);
    }

    /**
     * Get this Try's result, or throw its failure as an unchecked exception.
     *
     * @return the successful result of this Try
     * @throws RuntimeException wrapping the failure as an unchecked exception
     */
    public Result get() {
        if (result == null) {
            throw new RuntimeException(failure);
        }
        return result;
    }

    /**
     * Transform the result of this Try.
     *
     * <p>If this Try is successful, the provided function will be applied to
     * the current result and a new Try of the destination type will be
     * returned.</p>
     * <p>If this Try is a failure, a new Try of the destination result type
     * containing the existing failure will be returned.</p>
     * <p>If this Try is successful but the mapping function throws an
     * exception, a failed Try of the destination result type will be
     * returned, containing that exception.</p>
     *
     * @param func A function mapping this Try's result type to a new one
     * @param <NewResult> the output type of the transformation function
     * @return a new Try of either the transformed result or existing failure
     */
    public <NewResult> Try<NewResult> map(Function<? super Result, ? extends NewResult> func) {
        if (result == null) { return Try.fail(this.failure); }
        return Try.attemptApply(func, result);
    }

    /**
     * Transform the result of this Try into a new Try, returning that.
     *
     * <p>The behavior of this function is similar to {@link #map(Function)},
     * except that func should return a Try, and this will be returned without
     * being wrapped further.</p>
     * <p>Any uncaught exceptions thrown by func will themselves be captured in
     * a Try.</p>
     *
     * @param func a function mapping this Try's result type to another Try
     * @param <NewResult> the result type of the Try produced by func
     * @return a Try of the destination result type
     */
    public <NewResult> Try<NewResult> flatMap(Function<? super Result, Try<NewResult>> func) {
        if (result == null) { return Try.fail(this.failure); }
        // This is kind of like Try.attemptApply but we don't wrap the result
        // of func.apply in Try.succeed.
        try {
            return func.apply(result);
        } catch (Exception e) {
            return Try.fail(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Try<?> aTry = (Try<?>) o;
        return Objects.equals(result, aTry.result) &&
                Objects.equals(failure, aTry.failure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, failure);
    }

    @Override
    public String toString() {
        if (result != null) {
            return "Try{result=" + result + "}";
        } else {
            return "Try{failure=" + failure + "}";
        }
    }
}
