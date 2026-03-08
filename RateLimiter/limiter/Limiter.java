package RateLimiter.limiter;

import RateLimiter.RateLimitResult;

public interface Limiter {
    RateLimitResult allow(String clientId);
}