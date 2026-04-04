package AmazonLocker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Locker {
    private final List<Compartment>compartments;
    private final Map<String, AccessToken>accessTokenMap;

    public Locker(List<Compartment>compartments){
        this.compartments = compartments;
        this.accessTokenMap = new HashMap<>();
    }

    public AccessToken deposit(Size size){
        Size[] availableSizes = {Size.SMALL, Size.MEDIUM, Size.LARGE};
        int idx = 0;
        for(int i=0;i<3;i++){
            if(availableSizes[i] == size){
                idx = i;
                break;
            }
        }
        Compartment desiredCompartment = null;
        for(int i = idx;i<3;i++){
            Size currentSize = availableSizes[i];
            for(Compartment compartment: compartments){
                if(compartment.size == currentSize && !compartment.occupied){
                    desiredCompartment = compartment;
                    break;
                }
            }
        }
        if(desiredCompartment == null){
            throw new RuntimeException("No available compartment is available");
        }
        desiredCompartment.open();
        desiredCompartment.markOccupied();
        AccessToken accessToken = new AccessToken(desiredCompartment);
        accessTokenMap.put(accessToken.code, accessToken);
        return accessToken;
    }

    public void pickup(String accessTokenCode){
        AccessToken accessToken = accessTokenMap.get(accessTokenCode);
        if(accessToken == null){
            throw new RuntimeException("Invalid AccessToken");
        }
        if(accessToken.expiresAt<System.currentTimeMillis()){
            throw new RuntimeException("AccessToken has been expired");
        }
        Compartment compartment = accessToken.compartment;
        compartment.open();
        compartment.markFree();
        accessTokenMap.remove(accessTokenCode);
    }

    public void openCompartmentsWithExpiredToken(){
        List<AccessToken>expiredAccessTokens = new ArrayList<>();

       for(Map.Entry<String,AccessToken>entry : accessTokenMap.entrySet()){
            AccessToken accessToken = entry.getValue();
            if(accessToken.expiresAt<System.currentTimeMillis()){
                expiredAccessTokens.add(accessToken);
            }
       }

       for(AccessToken expiredAccessToken: expiredAccessTokens){
            Compartment compartment = expiredAccessToken.compartment;
            compartment.open();
            compartment.markFree();
            accessTokenMap.remove(expiredAccessToken.code);
       }
    }
}
