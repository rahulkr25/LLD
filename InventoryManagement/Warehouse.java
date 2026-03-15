package InventoryManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Warehouse {
    private String id;
    private HashMap<String, Integer> inventory;
    private HashMap<String, List<AlertConfig>>alertConfigs;
    
    public Warehouse(String id){
        this.id = id;
        this.inventory = new HashMap<>();
        this.alertConfigs = new HashMap<>();
    }

    public synchronized void addStock(String productId, int quantity){
        // since we are using syncroinized at method/class level for all the public apis, 
        // that means multiple threads cant access this method concurrently, 
        // hence we dont need concurrent hashmap, if it would have been that lock existed on product level
        // then the inventory map would have to be threadsafe concurrent hashmap implementation.
        int existingQuantity = inventory.getOrDefault(productId,0);
        inventory.put(productId, existingQuantity + quantity);

    }

    public boolean removeStock(String productId, int quantity){
        List<AlertListener> alertListeners = new ArrayList<>();
        int newQuanity;
        synchronized(this){
            int existingQuantity = inventory.getOrDefault(productId,0);
            if(existingQuantity < quantity){
                return false;
            }
            newQuanity = existingQuantity - quantity;
            inventory.put(productId, newQuanity);
            alertListeners = getAlertsToFire(productId, existingQuantity, newQuanity);
        }
        for(AlertListener alertListener : alertListeners){
            alertListener.onStockLowAlert(productId, this.id, newQuanity);
        }
        return true;
    }

    private List<AlertListener> getAlertsToFire(String productId, int oldQuantity, int newQuanity){
        List<AlertListener>alertListeners = new ArrayList<>();
        if(!alertConfigs.containsKey(productId)){
            return alertListeners;
        }
        for(AlertConfig alertConfig: alertConfigs.get(productId)){
            int threshold = alertConfig.threshold;
            
            if(oldQuantity>=threshold && newQuanity < threshold){
                alertListeners.add(alertConfig.alertListener);
            }
        }
        return alertListeners;
    }

    public synchronized boolean checkAvailability(String productId, int quantity){
        if(!inventory.containsKey(productId))return false;
        return inventory.get(productId) >= quantity;
    }

    public synchronized void setLowStockAlert(String productId, int threshold, AlertListener listener){
        if(!inventory.containsKey(productId)){
            throw new RuntimeException("product with given id: " + productId + "doesn't exist");
        }
        List<AlertConfig> existingAlertConfigs = alertConfigs.getOrDefault(productId, new ArrayList<>());
        existingAlertConfigs.add(new AlertConfig(listener, threshold));
    }

    private class AlertConfig {
       AlertListener alertListener;
       int threshold;
       public AlertConfig(AlertListener alertListener,  int threshold){
           this.threshold = threshold;
           this.alertListener = alertListener;
       }
    }
}
