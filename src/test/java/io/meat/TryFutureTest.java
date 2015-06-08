package io.meat;

import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TryFutureTest {
    @Test
    public void tryWrapCompletableFutureSucceeds() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();
        CompletableFuture<Try<String>> wrappedFuture = Try.wrapFuture(future);
        future.complete("It worked!");
        assertEquals("A successful wrapped future should contain the future's result as a successful Try",
                Try.succeed("It worked!"),
                wrappedFuture.get());
    }

    @Test
    public void tryWrapCompletableFutureFails() throws Exception {
        IllegalStateException error = new IllegalStateException("Something broke!");
        CompletableFuture<String> future = new CompletableFuture<>();
        CompletableFuture<Try<String>> wrappedFuture = Try.wrapFuture(future);
        future.completeExceptionally(error);
        assertEquals("A failed wrapped future should contain a failed Try",
                Try.fail(error),
                wrappedFuture.get());
    }
}
