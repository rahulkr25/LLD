package InventoryManagement.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import InventoryManagement.AlertListener;
import InventoryManagement.Warehouse;

public class InventoryManager {
    private  HashMap<String, Warehouse>existingWarehouse;
    private HashMap<String, Transfer>existingTransfers;
    
    public InventoryManager(List<String>warehouseIds){
        this.existingWarehouse = new HashMap<>();
        this.existingTransfers = new HashMap<>();
        for(String id: warehouseIds){
            existingWarehouse.put(id, new Warehouse(id));
        }
    }

    // Returns TransferId
     public String initiateTransfer(String fromWarehouseId, String toWarehouseId, String productId, int quantity){
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
        synchronized(fromWarehouse){
                if(!fromWarehouse.checkAvailability(productId, quantity)){
                    throw new RuntimeException("product with the given quanity: " + quantity + " doesnt exist in  " + fromWarehouseId);
                }
                fromWarehouse.removeStock(productId, quantity);
                Transfer transfer = new Transfer(UUID.randomUUID().toString(), fromWarehouseId, toWarehouseId, productId, quantity);
                existingTransfers.put(transfer.getId(), transfer);
                return transfer.getId();
        }

    }
    public void completeTransfer(String transferId){
        Transfer transfer = existingTransfers.get(transferId);
        if(transfer == null){
            throw new RuntimeException("transfer with given id: " + transferId + " doesn't exists");
        }
        Warehouse toWarehouse = existingWarehouse.get(transfer.getToWareHouseId());
        synchronized(toWarehouse){
            toWarehouse.addStock(transfer.getProductId(), transfer.getQuantity());
            existingTransfers.remove(transferId);
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

   

    public void setLowStockAlert(String productId, String warehouseId, int threshold, AlertListener listener){
        Warehouse warehouse = existingWarehouse.get(warehouseId);
        if(warehouse == null){
            throw new RuntimeException("warehouse with the given id: " + warehouseId + " doesn't exists.");
        }
        warehouse.setLowStockAlert(productId, threshold, listener);
    }
    
}
