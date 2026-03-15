package InventoryManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryManager {
    private  HashMap<String, Warehouse>existingWarehouse;
    
    public InventoryManager(List<String>warehouseIds){
        this.existingWarehouse = new HashMap<>();
        for(String id: warehouseIds){
            existingWarehouse.put(id, new Warehouse(id));
        }
    }

    public void addStock(String productId, String warehouseId, int quantity){
        Warehouse warehouse = existingWarehouse.get(warehouseId);
        if(warehouse == null){
            throw new RuntimeException("Warehouse with given  id:  " + warehouseId + " doesnt exist");
        }
        warehouse.addStock(productId, quantity);
    }

    public boolean removeStock(String productId, String warehouseId, int quantity){
         Warehouse warehouse = existingWarehouse.get(warehouseId);
        if(warehouse == null){
            throw new RuntimeException("Warehouse with given  id:  " + warehouseId + " doesnt exist");
        }
        return warehouse.removeStock(productId, quantity);
    }

    public List<Warehouse> getWarehousesWithAvailability(String productId, int quantity){
        List<Warehouse>warehouses = new ArrayList<>();
        for(Map.Entry<String, Warehouse> entry: existingWarehouse.entrySet()){
            if(entry.getValue().checkAvailability(productId, quantity)){
                warehouses.add(entry.getValue());
            }
        }
        return warehouses;
    }

    public boolean transfer(String fromWarehouseId, String toWarehouseId, String productId, int quantity){
        Warehouse fromWarehouse = existingWarehouse.get(fromWarehouseId);
        Warehouse toWarehouse = existingWarehouse.get(toWarehouseId);

        if(fromWarehouse == null){
            throw new RuntimeException("warehouse with the given id: " + fromWarehouseId + " doesn't exists.");
        }
        if(toWarehouse == null){
            throw new RuntimeException("warehouse with the given id: " + toWarehouseId + " doesn't exists.");
        }
        if(fromWarehouseId == toWarehouseId){
            throw new RuntimeException("both warehouse cannot be same");
        }
        if(quantity<=0){
            throw new RuntimeException("quantity given is invalid");
        }

        Warehouse firstLock =  (fromWarehouseId.compareTo(toWarehouseId) < 0 ? fromWarehouse: toWarehouse);
        Warehouse secondLock = (fromWarehouseId.compareTo(toWarehouseId) < 0 ? toWarehouse: fromWarehouse);

        synchronized(firstLock){
            synchronized(secondLock){
                if(!fromWarehouse.checkAvailability(productId, quantity)){
                    throw new RuntimeException("product with the given quanity: " + quantity + " doesnt exist in  " + fromWarehouseId);
                 }

                fromWarehouse.removeStock(productId, quantity);
                toWarehouse.addStock(productId, quantity);
            }
        }
        return true;
    }

    public void setLowStockAlert(String productId, String warehouseId, int threshold, AlertListener listener){
        Warehouse warehouse = existingWarehouse.get(warehouseId);
        if(warehouse == null){
            throw new RuntimeException("warehouse with the given id: " + warehouseId + " doesn't exists.");
        }
        warehouse.setLowStockAlert(productId, threshold, listener);
    }
    
}
