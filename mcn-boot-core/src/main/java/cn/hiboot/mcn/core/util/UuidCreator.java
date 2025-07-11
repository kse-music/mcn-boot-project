package cn.hiboot.mcn.core.util;

import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;

/**
 * UuidCreator
 *
 * @author DingHao
 * @since 2025/7/11 11:17
 */
public final class UuidCreator {

    private static final SecureRandom random = new SecureRandom();

    public static UUID generate() {
        long timestamp = System.currentTimeMillis();
        long rand12 = random.nextInt(1 << 12) & 0xFFFL;
        long msb = (timestamp & 0xFFFFFFFFFFFFL) << 16;
        msb |= (0x7L << 12) | rand12;
        return new UUID(msb, random.nextLong());
    }

    public static String generateString() {
        return generate().toString();
    }

    public static long getTimeInMillis(String uuid) {
        return extractUnixMillis(UUID.fromString(uuid));
    }

    public static Date getTime(String uuid) {
        return new Date(getTimeInMillis(uuid));
    }

    private static long extractUnixMillis(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        return (msb >>> 16) & 0xFFFFFFFFFFFFL;
    }

}
