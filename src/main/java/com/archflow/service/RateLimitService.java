package com.archflow.service;

import java.time.Duration;
import java.time.Instant;
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
    private static final int MAX_TRACKED_CLIENTS = 10_000;
    private static final Duration IDLE_BUCKET_TTL = Duration.ofHours(1);
    private final Map<String, ClientBucket> buckets = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientIp) {
        evictIdleBucketsIfNeeded();
        if (!buckets.containsKey(clientIp) && buckets.size() >= MAX_TRACKED_CLIENTS) {
            return false;
        }
        ClientBucket clientBucket = buckets.computeIfAbsent(clientIp, ignored -> new ClientBucket(newBucket()));
        clientBucket.lastAccess = Instant.now();
        return clientBucket.bucket.tryConsume(1);
    }

    private Bucket newBucket() {
        Refill refill = Refill.greedy(MAX_REQUESTS, REFILL_DURATION);
        Bandwidth limit = Bandwidth.classic(MAX_REQUESTS, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private void evictIdleBucketsIfNeeded() {
        if (buckets.size() < MAX_TRACKED_CLIENTS) {
            return;
        }
        Instant cutoff = Instant.now().minus(IDLE_BUCKET_TTL);
        buckets.entrySet().removeIf(entry -> entry.getValue().lastAccess.isBefore(cutoff));
    }

    private static final class ClientBucket {
        private final Bucket bucket;
        private volatile Instant lastAccess = Instant.now();

        private ClientBucket(Bucket bucket) {
            this.bucket = bucket;
        }
    }
}
