package AmazonLocker;

import java.time.Duration;
import java.util.UUID;

public class AccessToken {
    String code;
    long expiresAt;
    Compartment compartment;

    public AccessToken(Compartment compartment){
        this.code = UUID.randomUUID().toString();
        this.expiresAt = System.currentTimeMillis() + Duration.ofDays(7).toMillis();
        this.compartment = compartment;
    }
}
