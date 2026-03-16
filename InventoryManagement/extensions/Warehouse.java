package InventoryManagement.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import InventoryManagement.AlertListener;

public class Warehouse {
    private String id;
    private HashMap<String, Integer> inventory;
    // reserved map explicity for faster lookup in check availability method
    private HashMap<String, Integer> reserved;
    private HashMap<String, Reservation> reservations;
    private HashMap<String, List<AlertConfig>>alertConfigs;
    
    public Warehouse(String id){
        this.id = id;
        this.reserved = new HashMap<>();
        this.reservations = new HashMap<>();
        this.inventory = new HashMap<>();
        this.alertConfigs = new HashMap<>();
    }

    //When a customer starts checkout, we call reserveStock:
    public synchronized boolean reserveStock(String productId, int quanity, String reservationId, long timeoutMs){
        int totalQuantity = inventory.getOrDefault(productId, 0);
        int reservedQuanity = reserved.getOrDefault(productId, 0);
        int availableQuanity = totalQuantity - reservedQuanity;
        if(quanity>availableQuanity){
            return false;
        }
        Reservation reservation = new Reservation(reservationId, productId, quanity, System.currentTimeMillis() + timeoutMs);
        reservations.put(reservationId, reservation);
        reserved.put(productId, quanity + reservedQuanity);
        return true;
    }
    //The reservation gets stored with its expiration time. If payment succeeds, we confirm:
    public synchronized boolean confirmReservation(String reservationId){
        Reservation reservation = reservations.get(reservationId);
        if(reservation == null){
            throw new RuntimeException("no reservation exists for given reservationId: "+ reservationId);
        }
        if(reservation.expiresAt < System.currentTimeMillis()){
            throw new RuntimeException("reservation with given id: " + reservationId + " has been expired");
        }
        removeStock(reservation.productId, reservation.quanity);
        reserved.put(reservation.productId, reserved.get(reservation.productId) - reservation.quanity);
        reservations.remove(reservationId);
        return true;
    }

    //If checkout is abandoned or times out, we release:
    public synchronized boolean cancelReservation(String reservationId){
          Reservation reservation = reservations.get(reservationId);
        if(reservation == null){
            throw new RuntimeException("no reservation exists for given reservationId: "+ reservationId);
        }
        reserved.put(reservation.productId, reserved.get(reservation.productId) - reservation.quanity);
        reservations.remove(reservationId);
        return true;
    }

    private synchronized void cleanupExpiredReservations() throws InterruptedException{
        while(true){
            Thread.sleep(60000);
            long now = System.currentTimeMillis();

            for(String reservationId: reservations.keySet()){
                Reservation reservation = reservations.get(reservationId);
                if(reservation.expiresAt < now){
                    cancelReservation(reservationId);
                }
            }
        
        }
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
        int existingInventory = inventory.getOrDefault(productId,0);
        int reservedInventory = reserved.getOrDefault(productId, 0);
        return ((existingInventory - reservedInventory)>=quantity);
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

    private class Reservation {
        String id;
        String productId;
        int quanity;
        long expiresAt;
        public Reservation(String id, String productId, int quanity, long expiresAt){
            this.id = id;
            this.productId = productId;
            this.quanity = quanity;
            this.expiresAt = expiresAt;
        }
    }
}
