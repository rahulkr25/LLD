package RateLimiter.limiter;

import java.util.concurrent.ConcurrentHashMap;

import RateLimiter.RateLimitResult;

public class TokenBucketLimiter implements Limiter{
    private final int capacity;
    private final int refillRatePerSecond; 
    private final ConcurrentHashMap<String, TokenBucket> buckets;

    public TokenBucketLimiter(int capacity, int refillRatePerSecond){
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.buckets = new ConcurrentHashMap<>();
    }

    @Override
    public RateLimitResult allow(String clientId){
        // Atomically get or create bucket
        TokenBucket bucket = buckets.computeIfAbsent(clientId, k -> new TokenBucket(capacity, System.currentTimeMillis()));
        synchronized(bucket){
            long now = System.currentTimeMillis();
            double tokensToAdd = ((now - bucket.lastRefillTime) * refillRatePerSecond) / 1000;
            bucket.tokens = Math.min(capacity, bucket.tokens + tokensToAdd);
            bucket.lastRefillTime = now;

            if(bucket.tokens >=1){
                bucket.tokens -=1;
                int remaining = (int)Math.floor(bucket.tokens);
                return new RateLimitResult(true, remaining, null);
            }

            double tokensNeeded = 1 - bucket.tokens;
            long retryAfterMs = (long)Math.ceil((tokensNeeded * 1000)/(refillRatePerSecond));
            return new RateLimitResult(false, 0, retryAfterMs);
        }
    }

    private static class TokenBucket {
        double tokens;
        long lastRefillTime;

        public TokenBucket(double tokens, long lastRefillTime){
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
    }

    /**
    updateConfig(algoConfig)
        newCapacity = algoConfig["capacity"]
        newRefillRatePerSecond = algoConfig["refillRatePerSecond"]
        
        // Update parameters
        this.capacity = newCapacity
        this.refillRatePerSecond = newRefillRatePerSecond
        
        // Adjust existing buckets to respect new capacity
        for bucket in buckets.values()
            if bucket.tokens > newCapacity
                bucket.tokens = newCapacity
    */

}