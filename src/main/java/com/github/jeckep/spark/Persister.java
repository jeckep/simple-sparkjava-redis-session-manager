package com.github.jeckep.spark;

import java.io.Serializable;
import java.util.Map;

public interface Persister {
    void save(String sessionCookie, Map<String, Serializable> sessionAttrs, int expire);
    Map<String, Serializable> restore(String sessionCookie, int expire);
}