package com.project.inventory.configuration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global Cache Configuration — Temporary Caffeine Cache
 * 
 * TODO: Re-enable Redis caching when dependency issue is resolved
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Temporary: In-memory cache manager (Caffeine-based)
     * This will be replaced with Redis when dependency is fixed
     */
    @Bean
    public CacheManager cacheManager() {
        return new org.springframework.cache.concurrent.ConcurrentMapCacheManager("products", "userOrders");
    }
}
