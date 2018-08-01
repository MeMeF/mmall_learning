package com.sgm.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX = "token_";

    //goole的一种缓存类
    public static LoadingCache<String,String> localcache = CacheBuilder.newBuilder().initialCapacity(1000)
            .maximumSize(10000).build(new CacheLoader<String, String>() {
                    @Override
                //默认的数据加载实现,当调用get取值的时候,如果key没有对应的值,就调用这个方法进行加载.
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    public static void setKey(String key,String value){
        localcache.put(key,value);
    }

    public static String getKey(String key){
        String value = null;
        try {
            value = localcache.get(key);
            if ("null".equals(value)){
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            logger.error("localCache get Error",e);
        }
        return null;
    }
}
