package com.wlh.seckill.util;

/**
 * Twitter's distributed self-incrementing ID snowflake algorithm snowflake (Java Edition)
 **/
public class SnowFlake {
    /**
     * start timestamp
     */
    private final static long START_STMP = 1480166465631L;

    /**
     * The number of bits occupied by each part
     */
    private final static long SEQUENCE_BIT = 12;
    private final static long MACHINE_BIT = 5;
    private final static long DATACENTER_BIT = 5;

    /**
     * Maximum value for each part
     */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * displacement of each part to the left
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long datacenterId;
    private long machineId;
    private long sequence = 0L;
    private long lastStmp = -1L;

    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * generate next id
     * @return
     */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            //相同毫秒內，序號自增
            // Within the same millisecond, the serial number is automatically incremented
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列數已經達到最大
            //The maximum number of sequences in the same millisecond has been reached
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒內，序號置為0
            //In different milliseconds, the sequence number is set to 0
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT // timestamp part
                | datacenterId << DATACENTER_LEFT       // Data Center part
                | machineId << MACHINE_LEFT             // Machine Identification part
                | sequence;                             // serial number part
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake snowFlake = new SnowFlake(2, 1);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            System.out.println(snowFlake.nextId());
        }
        // System.out.println("total time：" + (System.currentTimeMillis() - start));
        System.out.println("Total time spent：" + (System.currentTimeMillis() - start));
    }
}
