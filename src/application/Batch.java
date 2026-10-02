package application;

public class Batch {

    private int id;
    private int productId;
    private int warehouseId;
    private int quantity;
    private String expiryDate;
    private String storageLocation;

    public Batch(int id, int productId, int warehouseId, int quantity,
                 String expiryDate, String storageLocation) {

        this.id = id;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.storageLocation = storageLocation;
    }

    public Batch(int productId, int warehouseId, int quantity,
                 String expiryDate, String storageLocation) {

        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.storageLocation = storageLocation;
    }

    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getStorageLocation() {
        return storageLocation;
    }
}