package com.qinyun.bbs.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author everr
 */
@Configuration
@EnableCaching
public class CaffeineConfig {

    private static final int DEFAULT_MAXSIZE = 10000;
    private static final int DEFAULT_TTL     = 600;

    /**
     * 定义cache名称、超时时长（秒）、最大容量
     * 每个cache缺省：10分钟超时、最多缓存10000条数据，需要修改可以在构造方法的参数中指定。
     */
    public enum Caches {
        bbsTopic(1800),
        bbsPost(1800),
        bbsReply(1800),
        bbsTopicMessageCount(1800),
        bbsTopicMessageList(1800),
        bbsTopicPage(1800),
        bbsHotTopicPage(1800),
        bbsNiceTopicPage(1800),
        bbsPostPage(1800),
        bbsLatestPost(1800),
        bbsFirstPost(1800),
        postSupport(3600),
        allModule(3600),
        module(36000),
        fallbackQuery(60, 20),
        ;

        Caches() {
        }

        Caches(int ttl) {
            this.ttl = ttl;
        }

        Caches(int ttl, int maxSize) {
            this.ttl = ttl;
            this.maxSize = maxSize;
        }

        int ttl     = DEFAULT_TTL;        //过期时间（秒）
        int maxSize = DEFAULT_MAXSIZE;    //最大數量
    }

    /**
     * 创建基于Caffeine的Cache Manager
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        List<CaffeineCache> caches = Arrays.stream(Caches.values())
                .map(cache -> new CaffeineCache(
                        cache.name(),
                        Caffeine.newBuilder()
                                .recordStats()
                                .expireAfterWrite(cache.ttl, TimeUnit.SECONDS)
                                .maximumSize(cache.maxSize)
                                .build()))
                .collect(Collectors.toList());
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}
