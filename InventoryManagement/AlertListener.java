package InventoryManagement;

public interface AlertListener {
    public void onStockLowAlert(String productId, String warehouseId, int currentQuantity);
}
