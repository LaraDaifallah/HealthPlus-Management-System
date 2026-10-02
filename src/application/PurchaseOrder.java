package application;

public class PurchaseOrder {

    private int poNumber;
    private int supplierId;
    private int employeeId;
    private String orderDate;
    private String expDeliveryDate;
    private String status;
    private double totalAmount;

    public PurchaseOrder(int poNumber, int supplierId, int employeeId,
                         String orderDate, String expDeliveryDate,
                         String status, double totalAmount) {
        this.poNumber = poNumber;
        this.supplierId = supplierId;
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.expDeliveryDate = expDeliveryDate;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public PurchaseOrder(int supplierId, int employeeId,
                         String orderDate, String expDeliveryDate,
                         String status, double totalAmount) {
        this.supplierId = supplierId;
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.expDeliveryDate = expDeliveryDate;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public int getPoNumber() { return poNumber; }
    public int getSupplierId() { return supplierId; }
    public int getEmployeeId() { return employeeId; }
    public String getOrderDate() { return orderDate; }
    public String getExpDeliveryDate() { return expDeliveryDate; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
}