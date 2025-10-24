package cn.hiboot.mcn.core.cache;

/**
 * CacheItem
 *
 * @author DingHao
 * @since 2025/10/24 10:01
 */
record CacheItem<V>(V value, long expireAt) {

    boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    boolean isExpired(long now) {
        return expireAt > 0 && now >= expireAt;
    }

}

