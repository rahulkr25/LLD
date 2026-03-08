### Description
A rate limiter controls how many requests a client can make to an API within a specific time window. When a request comes in, the rate limiter checks if the client has exceeded their quota. If they're under the limit, the request proceeds. If they've hit the cap, the request gets rejected. This protects APIs from abuse and ensures fair resource allocation across clients.
```java
{
  "endpoint": "/search",
  "algorithm": "TokenBucket",
  "algoConfig": {
    "capacity": 1000,
    "refillRatePerSecond": 10
  }
}
```
This config allows bursts up to 1000 requests, refilling at 10 requests per second.
Your job is to build the in-memory rate limiter that enforces these rules."


### Final Requirement
Requirements:
1. Configuration is provided at startup (loaded once)
2. System receives requests with (clientId: string, endpoint: string)
3. Each endpoint has a configuration specifying:
   - Algorithm to use (e.g., "TokenBucket", "SlidingWindowLog", etc.)
   - Algorithm-specific parameters (e.g., capacity, refillRatePerSecond for Token Bucket)
4. System enforces rate limits by checking clientId against the endpoint's configuration
5. Return structured result: (allowed: boolean, remaining: int, retryAfterMs: long | null)
6. If endpoint has no configuration, use a default limit

#### Out of scope:
- Distributed rate limiting (Redis, coordination)
- Dynamic configuration updates
- Metrics and monitoring
- Config validation beyond basic checks


### Entities
1. RateLimiter	: The orchestrator and entry point for the system. It receives incoming requests with client IDs and endpoints, looks up the appropriate endpoint configuration, and delegates to the right algorithm instance to make the rate limiting decision. It owns the collection of algorithm instances (one per configured endpoint) and handles fallback to a default configuration when requests hit endpoints we don't have specific rules for.
2. Limiter (interface) : Defines the contract that all rate limiting algorithms must follow. The interface has a single method: allow(key) which returns a RateLimitResult. Each concrete algorithm—Token Bucket, Sliding Window Log, etc.—implements this interface with its own logic and per-key state management approach.
3 : RateLimitResult	A value object that packages up the rate limiting decision along with metadata. It contains three fields: whether the request is allowed, how many requests remain in the quota, and how many milliseconds until retry if denied. Once created, it's immutable.


### Class Design
```java
class RateLimiter:
   - limiters: Map<String, Limiter>
   - defaultLimiter : Limiter

   + RameLimiter(config, defaultConfig)
   + allow(clientId, endpoint) -> RateLimitResult

class LimiterFactory: 
   + create(config) -> Limiter

interface Limiter:
   + allow(clientId) -> RateLimitResult

class RateLimitResult:
   - allowed: boolean
   - remaining: int
   - retryAfterMs: long | null

   + RateLimitResult(allowed, remaining, retryAfterMs)

class TokenBucketLimiter implements Limiter:
   - capacity: int
   - refillRatePerSecond: int
   - buckets: Map<String, TokenBucket>

   + allow(clientId)

class TokenBucket:
   - tokens: double
   - lastRefillTime: long


class SlidingWindowLogLimiter implements Limiter:
   - maxRequests: int
   - windowMs: long
   - logs: Map<String, RequestLog>


class RequestLog:
   - timeStamps: Queue<long>
```