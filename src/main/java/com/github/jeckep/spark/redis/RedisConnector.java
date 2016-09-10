package com.github.jeckep.spark.redis;

public interface RedisConnector {
    byte[] get(byte[] key);
    Long expire(final byte[] key, final int seconds);
    String set(final byte[] key, final byte[] value);
    Long del(byte[] key);
}
