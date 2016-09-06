package com.jeckep.chat.session.persist.redis;

public interface RedisConnector {
    byte[] get(byte[] key);
    Boolean exists(final byte[] key);
    Long expire(final byte[] key, final int seconds);
    String set(final byte[] key, final byte[] value);
}
