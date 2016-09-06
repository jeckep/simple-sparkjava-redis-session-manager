package com.jeckep.chat.session.persist.redis;

import redis.clients.jedis.BinaryJedisCommands;

public class JedisConnector implements RedisConnector {
    private BinaryJedisCommands jedis;

    public JedisConnector(BinaryJedisCommands jedis) {
        this.jedis = jedis;
    }

    @Override
    public byte[] get(byte[] key) {
        return jedis.get(key);
    }

    @Override
    public Long expire(byte[] key, int seconds) {
        return jedis.expire(key, seconds);
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return jedis.set(key, value);
    }
}
