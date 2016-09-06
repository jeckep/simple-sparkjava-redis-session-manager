package com.jeckep.chat.session.persist.redis;

import com.jeckep.chat.session.persist.PSF;
import com.jeckep.chat.session.persist.Persister;
import spark.Session;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class RedisSimplePersister implements Persister {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RedisSimplePersister.class);
    private RedisConnector redis;

    public RedisSimplePersister(RedisConnector redis) {
        this.redis = redis;
    }

    @Override
    public Map<String, Object> restore(String sessionCookieValue, int expire) {
        byte[] value = redis.get(sessionCookieValue.getBytes());
        if (value != null) {
            redis.expire(sessionCookieValue.getBytes(), expire);
            try {
                Map<String, Object> attrs = (Map<String, Object>) convertFromBytes(value);
                return attrs;
            } catch (IOException | ClassNotFoundException e) {
                log.error("Cannot convert byte[] to session attrs", e);
            }
        }
        return new HashMap<>();
    }

    @Override
    public void save(String sessionCookie, Session session, int expire) {
        try {
            Map<String, Object> sessionAttrs = new HashMap<>();
            for(String key: session.attributes()){
                // do not store session cookie value in a map, because it is a key,
                // we can do it, but there is no need
                if(PSF.SESSION_COOKIE_NAME.equals(key)) continue;
                sessionAttrs.put(key, session.attribute(key));
            }
            byte[] value = convertToBytes(sessionAttrs);
            redis.set(sessionCookie.getBytes(), value);
            //no need to set expire on save to redis, because we do it in on every request when restore from redis
            //redis.expire(sessionCookieValue.getBytes(), expire);
        } catch (IOException e) {
            log.error("Cannot convert session attrs to byte[]", e);
        }
    }

    private byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }
}
