package application;

public class PurchaseOrderItem {

    private int poItemId;
    private int poNumber;
    private int productId;
    private int qtyOrdered;
    private double unitCost;
    private int qtyReceived;

    public PurchaseOrderItem(int poItemId, int poNumber, int productId,
                             int qtyOrdered, double unitCost, int qtyReceived) {
        this.poItemId = poItemId;
        this.poNumber = poNumber;
        this.productId = productId;
        this.qtyOrdered = qtyOrdered;
        this.unitCost = unitCost;
        this.qtyReceived = qtyReceived;
    }

    public PurchaseOrderItem(int poNumber, int productId,
                             int qtyOrdered, double unitCost, int qtyReceived) {
        this.poNumber = poNumber;
        this.productId = productId;
        this.qtyOrdered = qtyOrdered;
        this.unitCost = unitCost;
        this.qtyReceived = qtyReceived;
    }

    public int getPoItemId() { return poItemId; }
    public int getPoNumber() { return poNumber; }
    public int getProductId() { return productId; }
    public int getQtyOrdered() { return qtyOrdered; }
    public double getUnitCost() { return unitCost; }
    public int getQtyReceived() { return qtyReceived; }
}