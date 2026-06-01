package com.api.springcore.service;


import com.api.springcore.config.RateLimitProperties;
import com.api.springcore.filter.RateLimitFilter;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final Cache<String, Bucket> cache;
    private final RateLimitProperties props;

    public RateLimiterService(RateLimitProperties props) {


        this.props = props;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(props.getCacheExpireHours(), TimeUnit.HOURS)
                .build();
    }

    public Bucket resolveBucket(String key) {
        return cache.get(key, k -> createBucket());
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(
                props.getCapacity(),
                Refill.greedy(
                        props.getRefillTokens(),
                        Duration.ofMinutes(props.getRefillDurationMinutes())
                )
        );
        return Bucket.builder().addLimit(limit).build();
    }
}