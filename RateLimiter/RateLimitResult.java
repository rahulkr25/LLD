package RateLimiter;

public class RateLimitResult {
    private final boolean allowed;
    private final int remaining;
    private final Long retryAfterMs;

    public RateLimitResult(boolean allowed, int remaining, Long retryAfterMs){
       this.allowed = allowed;
       this.remaining = remaining;
       this.retryAfterMs = retryAfterMs;
    }
}
