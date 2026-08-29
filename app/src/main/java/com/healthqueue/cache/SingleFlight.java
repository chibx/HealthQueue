package com.healthqueue.cache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class SingleFlight<K, V> {
    private final ConcurrentMap<K, CompletableFuture<V>> inFlight = new ConcurrentHashMap<>();

    public V doCall(K key, java.util.function.Supplier<V> work) throws Exception {
        // computeIfAbsent is atomic. Only the first thread will trigger the
        // supplyAsync.
        CompletableFuture<V> future = inFlight.computeIfAbsent(key, k -> CompletableFuture.supplyAsync(() -> {
            try {
                return work.get();
            } finally {
                // This mimics Go's auto-forgetting behavior
                inFlight.remove(k);
            }
        }));

        // All threads (the one executing, and the ones waiting) block here until done
        return future.get();
    }
}