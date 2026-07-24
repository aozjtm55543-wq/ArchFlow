package com.archflow.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RateLimitServiceTest {

    @Test
    void allowsFiveRequestsPerClientAndRejectsTheSixth() {
        RateLimitService service = new RateLimitService();

        for (int attempt = 0; attempt < 5; attempt++) {
            assertTrue(service.allowRequest("203.0.113.10"));
        }
        assertFalse(service.allowRequest("203.0.113.10"));
    }

    @Test
    void tracksEachClientIndependently() {
        RateLimitService service = new RateLimitService();

        for (int attempt = 0; attempt < 5; attempt++) {
            service.allowRequest("203.0.113.10");
        }
        assertTrue(service.allowRequest("203.0.113.11"));
    }
}
