package RateLimiter.limiter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import RateLimiter.RateLimitResult;

public class SlidingWindowLogLimiter  implements Limiter{
    private final int maxRequests;
    private final long windowMs;
    private final HashMap<String, RequestLog>logs;

    public SlidingWindowLogLimiter(int maxRequests, long windowMs){
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
        this.logs = new HashMap<>();
    }

    @Override
    public RateLimitResult allow(String clientId){
        RequestLog requestLog = logs.computeIfAbsent(clientId, k ->  new RequestLog());
        
        long now = System.currentTimeMillis();
        long cutOff = now - windowMs;

        while(!requestLog.timeStamps.isEmpty() && requestLog.timeStamps.peekFirst() <= cutOff){
            requestLog.timeStamps.pollFirst();
        }

        if(requestLog.timeStamps.size()<maxRequests){
            requestLog.timeStamps.add(now);
            int remaining = maxRequests - requestLog.timeStamps.size();
            return new RateLimitResult(true, remaining, null);
        }

        long retryAfterMs = (requestLog.timeStamps.peekFirst() + windowMs) - now;
        return new RateLimitResult(false, 0, retryAfterMs);
    }

    private static class RequestLog{
        Deque<Long>timeStamps;
        public RequestLog(){
            this.timeStamps = new ArrayDeque<>();
        }
    }
    
}
