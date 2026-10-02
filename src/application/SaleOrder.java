package application;

public class SaleOrder {

    private int saleOrderId;
    private int clientId;
    private int employeeId;
    private String orderDate;
    private String deliveryDate;
    private String status;
    private double totalAmount;
    private String paymentStatus;

    public SaleOrder(int saleOrderId, int clientId, int employeeId,
                     String orderDate, String deliveryDate,
                     String status, double totalAmount, String paymentStatus) {
        this.saleOrderId = saleOrderId;
        this.clientId = clientId;
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
    }

    public SaleOrder(int clientId, int employeeId,
                     String orderDate, String deliveryDate,
                     String status, double totalAmount, String paymentStatus) {
        this.clientId = clientId;
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
    }

    public int getSaleOrderId() { return saleOrderId; }
    public int getClientId() { return clientId; }
    public int getEmployeeId() { return employeeId; }
    public String getOrderDate() { return orderDate; }
    public String getDeliveryDate() { return deliveryDate; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }
}