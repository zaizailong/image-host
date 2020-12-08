package com.tuituidan.oss.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tuituidan.oss.bean.ChunkFileResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ChunkFileCacheService {
    private static final int EXPIRE = 7;

    private static final int MAX_SIZE = 2048;

    /**
     * 内部的缓存对象.
     */
    private static final Cache<String, ChunkFileResult> CACHE = Caffeine.newBuilder()
            .expireAfterWrite(EXPIRE, TimeUnit.DAYS)
            .expireAfterAccess(EXPIRE, TimeUnit.DAYS)
            .maximumSize(MAX_SIZE)
            .build();

    public void put(String fileMd5, ChunkFileResult chunkFile){
        CACHE.put(fileMd5,chunkFile);
    }

    public ChunkFileResult get(String fileMd5){
        return CACHE.getIfPresent(fileMd5);
    }
}
