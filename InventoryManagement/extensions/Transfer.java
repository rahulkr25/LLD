package InventoryManagement.extensions;

import java.time.Instant;
import java.util.Date;

class Transfer {
    private String id;
    private String fromWarehouseId;
    private String toWarehouseId;
    private String productId;
    private int quanity;
    private Date createdAt;

    public Transfer(String id, String fromWarehouseId, String toWarehouseId, String productId, int quanity){
        this.id = id;
        this.fromWarehouseId = fromWarehouseId;
        this.toWarehouseId = toWarehouseId;
        this.productId = productId;
        this.quanity = quanity;
        this.createdAt = Date.from(Instant.now());
    }
    public String getId(){
        return this.id;
    }
    public String getToWareHouseId(){
        return this.toWarehouseId;
    }
    public String getProductId(){
        return this.productId;
    }
    public int getQuantity(){
        return this.quanity;
    }
}