Simple Redis Session Manager for sparkjava
==============================================

Simple session manager for [sparkjava](https://github.com/perwendel/spark). It retrieves all session attributes from Redis on every request, and save them back to Redis if session attributes were changed during request processing.

Getting started
---------------

```java
public static void main(String[] args) {
   final PSF psf = new PSF(new RedisSimplePersister(new JedisConnector(jedis)));
   
   //before all before-filters                 
   before("*",                  psf.getBeforeFilter());
   // before-filters, routes, after-filters 
   
   //after all after filters
   after("*",                   psf.getAfterFilter());
}
```


Customisation
-------------

If you don't want to use [jedis](https://github.com/xetorthio/jedis) as redis client,  create you own class implementing RedisConnector instead of JedisConnector.

Disclaimer
----------
Think before use this in production. You can use it but on your own risk.
