package com.archflow.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Service
public class RateLimitService {
    private static final int MAX_REQUESTS = 5;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientIp) {
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::newBucket);
        return bucket.tryConsume(1);
    }

    private Bucket newBucket(String clientIp) {
        Refill refill = Refill.greedy(MAX_REQUESTS, REFILL_DURATION);
        Bandwidth limit = Bandwidth.classic(MAX_REQUESTS, refill);
        return Bucket.builder().addLimit(limit).build();
    }
}
