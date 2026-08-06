package com.healthqueue.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

record GoReturn<T>(T value, Throwable error) {
}

public final class GoErrRet {

    /**
     * Wraps an async function so its CompletableFuture never fails outright —
     * it always resolves to GoReturn(value, null) or GoReturn(null, error).
     */
    @SuppressWarnings("null")
    public static <A, T> Function<A, CompletableFuture<GoReturn<T>>> toGoErrorRet(
            Function<A, CompletableFuture<T>> fn) {
        return arg -> fn.apply(arg).handle((result, error) -> error == null
                ? new GoReturn<>(result, null)
                : new GoReturn<>(null, unwrap(error)));
    }

    /**
     * Converts a GoReturn-shaped future back into a normal future that fails on
     * error.
     */
    public static <T> CompletableFuture<T> throwGoError(CompletableFuture<GoReturn<T>> future) {
        return future.thenApply(r -> {
            if (r.error() != null) {
                throw (r.error() instanceof RuntimeException re) ? re : new CompletionException(r.error());
            }
            return r.value();
        });
    }

    // CompletableFuture wraps thrown exceptions in CompletionException;
    // unwrap so callers see the real cause, like a plain `catch (error)` would in
    // JS.
    private static Throwable unwrap(Throwable t) {
        return (t instanceof CompletionException && t.getCause() != null) ? t.getCause() : t;
    }
}