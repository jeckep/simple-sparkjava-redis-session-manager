package com.github.jeckep.spark.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisThreadSafeConnector implements RedisConnector {
    private JedisPool pool;

    public JedisThreadSafeConnector(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public byte[] get(byte[] key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    public Long expire(byte[] key, int seconds) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.expire(key, seconds);
        }
    }

    @Override
    public String set(byte[] key, byte[] value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.set(key, value);
        }
    }
}
