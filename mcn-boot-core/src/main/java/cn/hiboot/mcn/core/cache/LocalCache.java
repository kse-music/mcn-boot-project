package cn.hiboot.mcn.core.cache;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * LocalCache
 *
 * @author DingHao
 * @since 2025/10/24 10:00
 */
public class LocalCache<K, V> {

    private final ConcurrentHashMap<K, CacheItem<V>> cache = new ConcurrentHashMap<>();

    public void put(K key, V value) {
        cache.put(key, new CacheItem<>(value, -1));
    }

    public void put(K key, V value, Duration ttl) {
        if (ttl == null) {
            put(key, value);
            return;
        }
        put(key, value, ttl.toMillis());
    }

    public void put(K key, V value, long ttlMillis) {
        long expireAt = System.currentTimeMillis() + ttlMillis;
        cache.put(key, new CacheItem<>(value, expireAt));
    }

    public V get(K key) {
        return get(key, null);
    }

    public V get(K key, Supplier<V> supplier) {
        return get(key, supplier, null);
    }

    public V get(K key, Supplier<V> supplier, Duration ttl) {
        CacheItem<V> item = cache.get(key);
        if (item == null || item.isExpired()) {
            cache.remove(key);
            if (supplier == null) {
                return null;
            }
            V value = supplier.get();
            put(key, value, ttl);
            return value;
        }
        return item.value();
    }

    public void remove(K key) {
        cache.remove(key);
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        for (Map.Entry<K, CacheItem<V>> entry : cache.entrySet()) {
            if (entry.getValue().isExpired(now)) {
                cache.remove(entry.getKey());
            }
        }
    }

}
