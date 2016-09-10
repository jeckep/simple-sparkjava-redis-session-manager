Simple Redis Session Manager for sparkjava
==============================================

[![License badge](http://img.shields.io/badge/license-Apache%202.0-green.svg?style=flat)](https://raw.githubusercontent.com/jeckep/simple-sparkjava-redis-session-manager/master/LICENSE.txt)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.jeckep/sparkjava-redis-session-manager.svg)]()

Simple session manager for [spark](https://github.com/perwendel/spark). It retrieves all session attributes from Redis on every request, and save them back to Redis if session attributes were changed during request processing.

Getting started
---------------

Add to your pom.xml:

```xml
<dependency>
    <groupId>com.github.jeckep</groupId>
    <artifactId>sparkjava-redis-session-manager</artifactId>
    <version>0.2.0</version>
</dependency>
```

Add to your main app class:

```java
public static void main(String[] args) {
   final JedisPool pool = new JedisPool(new JedisPoolConfig(), "redis");
   final PSF psf = new PSF(new RedisSimplePersister(new JedisThreadSafeConnector(pool)));
   
   //before all before-filters                 
   before("*", psf.getBeforeFilter());
   
   //after all after-filters
   after("*", psf.getAfterFilter());
}
```


Customisation
-------------

If you don't want to use [jedis](https://github.com/xetorthio/jedis) as redis client,  create you own class implementing RedisConnector instead of JedisConnector.

Disclaimer
----------
Think before use this in production. You can use it but on your own risk.
