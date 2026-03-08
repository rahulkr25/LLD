package RateLimiter.limiter;

import java.util.Map;

public class LimiterFactory {
    @SuppressWarnings("unchecked")
    public Limiter create(Map<String,Object>config){
        String endpoint = (String)config.get("enpoint");
        String algorithm = (String)config.get("algorithm");
        Map<String, Object> algoConfig = (Map<String,Object>)config.get("algoConfig");

        if(algorithm == null || algoConfig == null){
            throw new RuntimeException("Invalid config for endpoint : " + endpoint);
        }

        if("TokenBucket".equals(algorithm)){
            int capacity = (int)algoConfig.getOrDefault("capacity", 0);
            int refillRatePerSecond = (int)algoConfig.getOrDefault("refillRatePerSecond", 0);
            return new TokenBucketLimiter(capacity, refillRatePerSecond);
        }

        if("SlidingWindowLog".equals(algorithm)){
            int maxRequests = (int)algoConfig.getOrDefault("maxRequests", 0);
            long windowMs = (long)algoConfig.getOrDefault("windowMs", 0);
            return new SlidingWindowLogLimiter(maxRequests, windowMs);
        }

        throw new RuntimeException("Invalid algorithm for endpoint : " + endpoint);
    }
}