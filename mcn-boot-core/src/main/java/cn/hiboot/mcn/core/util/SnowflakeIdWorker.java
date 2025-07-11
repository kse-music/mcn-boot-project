package cn.hiboot.mcn.core.util;

import java.util.Date;

/**
 * SnowflakeId
 *
 * @author DingHao
 * @since 2024/5/29 16:05
 */
public class SnowflakeIdWorker {

    private static final long DEFAULT_TWEPOCH = 1704067200000L;
    private final long twepoch;

    // 机器id所占的位数
    private static final long workerIdBits = 5L;
    private static final long datacenterIdBits = 5L;

    private static final long sequenceBits = 12L;

    // 机器id左移12位
    private static final long workerIdShift = sequenceBits;
    private static final long datacenterIdShift = sequenceBits + workerIdBits;
    private static final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    // 生成序列的掩码，这里为4095
    private static final long sequenceMask = ~(-1L << sequenceBits);

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static final SnowflakeIdWorker snowflakeIdWorker = SnowflakeIdWorker.of(1, 1);

    private SnowflakeIdWorker(long workerId, long datacenterId, Date epochDate) {
        long maxWorkerId = ~(-1L << workerIdBits);
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        long maxDatacenterId = ~(-1L << datacenterIdBits);
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.twepoch = epochDate == null ? DEFAULT_TWEPOCH : epochDate.getTime();
    }

    static SnowflakeIdWorker getInstance() {
        return snowflakeIdWorker;
    }

    public static SnowflakeIdWorker of(long workerId, long datacenterId) {
        return new SnowflakeIdWorker(workerId, datacenterId, null);
    }

    public static SnowflakeIdWorker of(long workerId, long datacenterId, Date twepoch) {
        return new SnowflakeIdWorker(workerId, datacenterId, twepoch);
    }

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    public String nextIdString() {
        return Long.toString(nextId());
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    public Date createTime(long id) {
        long timestampPart = (id >> timestampLeftShift);
        long timestamp = timestampPart + twepoch;
        return new Date(timestamp);
    }

}

