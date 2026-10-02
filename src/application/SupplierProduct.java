package application;

public class SupplierProduct {

    private int supplierId;
    private int productId;
    private double unitCost;

    public SupplierProduct(int supplierId, int productId, double unitCost) {
        this.supplierId = supplierId;
        this.productId = productId;
        this.unitCost = unitCost;
    }
    public int getSupplierId() {
        return supplierId;
    }
    public int getProductId() {
        return productId;
    }
    public double getUnitCost() {
        return unitCost;
    }
}