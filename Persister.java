package com.jeckep.chat.session.persist;

import spark.Session;

import java.util.Map;

public interface Persister {
    void save(String sessionCookieValue, Session session, int expire);
    Map<String, Object> restore(String sessionCookie, int expire);
}