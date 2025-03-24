package com.qit.softwarestudio.singleflight.core;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * SingleFlight is a utility class for access suppression。
 *
 * @param <K> key type
 * @param <V> value type
 * @author KingYen.
 */
public class SingleFlight<K, V> {
    private final Map<K, Call<V>> inFlight = new ConcurrentHashMap<>();

    /**
     * Perform an operation, and if an operation with the same key is in progress, wait for it to complete and return the result
     *
     * @param key operating key
     * @param fn the actual operation performed
     * @return result of operation
     * @throws RuntimeException the exception occurs during operation execution
     */
    public V doOnce(K key, Supplier<V> fn) {
        try {
            return doOnceFuture(key, fn).join();
        } catch (Exception e) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }
                throw new RuntimeException("Error executing operation", e.getCause());
            }
            throw new RuntimeException("Error executing operation", e);
        }
    }

    /**
     * Execute an operation asynchronously, and if an operation with the same key is in progress, wait for it to complete and return the result
     *
     * @param key operating key
     * @param fn the actual operation performed
     * @return CompletableFuture containing the result of the operation
     */
    public CompletableFuture<V> doOnceFuture(K key, Supplier<V> fn) {
        // Quick path: Check whether an operation with the same key is in progress
        Call<V> existingCall = inFlight.get(key);
        if (existingCall != null) {
            return existingCall.future;
        }

        // create the new call object
        Call<V> call = new Call<>();

        // Put the new Call by atomic operation
        Call<V> previousCall = inFlight.putIfAbsent(key, call);
        if (previousCall != null) {
            // If another thread has already created a Call, return the future of that Call directly
            return previousCall.future;
        }

        // Perform the actual operation and complete the future
        try {
            V result = fn.get();
            call.future.complete(result);
        } catch (Throwable e) {
            call.future.completeExceptionally(e);
        } finally {
            // remove the call when the operation is complete
            inFlight.remove(key, call);
        }

        return call.future;
    }

    /**
     * Forcibly delete a key and cancel all waiting operations
     *
     * @param key the key to delete
     * @return Return true if key exists and is deleted, false otherwise
     */
    public boolean forget(K key) {
        Call<V> call = inFlight.remove(key);
        if (call != null && !call.future.isDone()) {
            call.future.completeExceptionally(new RuntimeException("Operation cancelled"));
            return true;
        }
        return false;
    }

    /**
     * Gets the number of operations currently in progress
     *
     * @return number of ongoing operations
     */
    public int pendingCount() {
        return inFlight.size();
    }

    /**
     * represents a call in progress
     */
    private static class Call<V> {
        final CompletableFuture<V> future = new CompletableFuture<>();
    }
}
