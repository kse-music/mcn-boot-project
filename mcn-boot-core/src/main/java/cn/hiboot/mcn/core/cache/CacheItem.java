package cn.hiboot.mcn.core.cache;

/**
 * CacheItem
 *
 * @author DingHao
 * @since 2025/10/24 10:01
 */
class CacheItem<V> {

    final V value;
    final long expireAt;

    CacheItem(V value, long expireAt) {
        this.value = value;
        this.expireAt = expireAt;
    }

    boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    boolean isExpired(long now) {
        return expireAt > 0 && now >= expireAt;
    }

}
