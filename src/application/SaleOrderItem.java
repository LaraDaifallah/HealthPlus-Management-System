package application;

public class SaleOrderItem {

    private int saleItemId;
    private int saleOrderId;
    private int batchId;
    private int qtyOrdered;
    private double unitPrice;
    private double discount;

    public SaleOrderItem(int saleItemId, int saleOrderId, int batchId,
                         int qtyOrdered, double unitPrice, double discount) {
        this.saleItemId = saleItemId;
        this.saleOrderId = saleOrderId;
        this.batchId = batchId;
        this.qtyOrdered = qtyOrdered;
        this.unitPrice = unitPrice;
        this.discount = discount;
    }

    public SaleOrderItem(int saleOrderId, int batchId,
                         int qtyOrdered, double unitPrice, double discount) {
        this.saleOrderId = saleOrderId;
        this.batchId = batchId;
        this.qtyOrdered = qtyOrdered;
        this.unitPrice = unitPrice;
        this.discount = discount;
    }

    public int getSaleItemId() { return saleItemId; }
    public int getSaleOrderId() { return saleOrderId; }
    public int getBatchId() { return batchId; }
    public int getQtyOrdered() { return qtyOrdered; }
    public double getUnitPrice() { return unitPrice; }
    public double getDiscount() { return discount; }
}