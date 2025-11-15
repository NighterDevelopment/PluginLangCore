package io.github.pluginlangcore.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A thread-safe LRU (Least Recently Used) cache implementation.
 * <p>
 * This cache automatically removes the least recently accessed entries
 * when the cache reaches its maximum capacity. All operations are
 * synchronized to ensure thread safety.
 * </p>
 *
 * @param <K> The type of keys maintained by this cache
 * @param <V> The type of values maintained by this cache
 * @author PluginLangCore Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class LRUCache<K, V> {
    private final LinkedHashMap<K, V> cache;
    private int capacity;

    /**
     * Constructs an LRU cache with the specified capacity.
     *
     * @param capacity The maximum number of entries in the cache (must be positive)
     * @throws IllegalArgumentException if capacity is not positive
     */
    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.cache = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LRUCache.this.capacity;
            }
        };
    }

    /**
     * Returns the value associated with the specified key,
     * or null if no mapping exists for the key.
     * <p>
     * This operation updates the access order, making this entry
     * the most recently used.
     * </p>
     *
     * @param key The key whose associated value is to be returned
     * @return The value associated with the key, or null if no mapping exists
     */
    public synchronized V get(K key) {
        return cache.get(key);
    }

    /**
     * Associates the specified value with the specified key in this cache.
     * <p>
     * If the cache previously contained a mapping for this key, the old
     * value is replaced. If adding this entry causes the cache to exceed
     * its capacity, the least recently used entry is removed.
     * </p>
     *
     * @param key   The key with which the specified value is to be associated
     * @param value The value to be associated with the specified key
     * @return The previous value associated with the key, or null if no mapping existed
     */
    public synchronized V put(K key, V value) {
        return cache.put(key, value);
    }

    /**
     * Removes all entries from the cache.
     * <p>
     * After this operation, the cache will be empty.
     * </p>
     */
    public synchronized void clear() {
        cache.clear();
    }

    /**
     * Returns the number of key-value mappings currently in this cache.
     *
     * @return The number of key-value mappings in this cache
     */
    public synchronized int size() {
        return cache.size();
    }

    /**
     * Returns the maximum capacity of this cache.
     *
     * @return The maximum number of entries this cache can hold
     */
    public synchronized int capacity() {
        return capacity;
    }

    /**
     * Resizes the cache capacity.
     * <p>
     * If the new capacity is smaller than the current number of entries,
     * the least recently used entries will be removed on the next put operation.
     * </p>
     *
     * @param newCapacity The new capacity for the cache (must be positive)
     * @throws IllegalArgumentException if newCapacity is not positive
     */
    public synchronized void resize(int newCapacity) {
        if (newCapacity <= 0) {
            throw new IllegalArgumentException("New capacity must be positive");
        }
        this.capacity = newCapacity;
    }

    /**
     * Checks if the cache contains a mapping for the specified key.
     *
     * @param key The key whose presence in this cache is to be tested
     * @return true if this cache contains a mapping for the specified key
     */
    public synchronized boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    /**
     * Removes the mapping for the specified key from this cache if present.
     *
     * @param key The key whose mapping is to be removed from the cache
     * @return The previous value associated with the key, or null if there was no mapping
     */
    public synchronized V remove(K key) {
        return cache.remove(key);
    }
}