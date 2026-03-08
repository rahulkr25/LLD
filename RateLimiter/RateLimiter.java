package RateLimiter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import RateLimiter.limiter.Limiter;
import RateLimiter.limiter.LimiterFactory;


public class RateLimiter {
    private final HashMap<String, Limiter> limiters;
    private final Limiter defaultLimiter;

    public RateLimiter(List<Map<String, Object>> configs, Map<String, Object> defaultConfig){
        this.limiters = new HashMap<>();
        LimiterFactory limiterFactory = new LimiterFactory();
        for(Map<String,Object> config: configs){
            String endpoint = (String)config.get("endpoint");
            if(endpoint == null){continue;}
            Limiter limiter = limiterFactory.create(config);
            limiters.put(endpoint, limiter);
        }
        this.defaultLimiter = limiterFactory.create(defaultConfig);
    }

    public RateLimitResult allow(String endpoint, String clientId){
        Limiter limiter = limiters.getOrDefault(endpoint, defaultLimiter);
        return limiter.allow(clientId);
    }
}
