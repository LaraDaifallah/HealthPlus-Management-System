package application;

public class Payment {

    private int paymentId;
    private Integer saleOrderId;
    private Integer poNumber;
    private String paymentDate;
    private double amount;
    private String paymentMethod;
    private String direction;

    public Payment(int paymentId, Integer saleOrderId, Integer poNumber,
                   String paymentDate, double amount,
                   String paymentMethod, String direction) {
        this.paymentId = paymentId;
        this.saleOrderId = saleOrderId;
        this.poNumber = poNumber;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.direction = direction;
    }

    public Payment(Integer saleOrderId, Integer poNumber,
                   String paymentDate, double amount,
                   String paymentMethod, String direction) {
        this.saleOrderId = saleOrderId;
        this.poNumber = poNumber;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.direction = direction;
    }

    public int getPaymentId() { return paymentId; }
    public Integer getSaleOrderId() { return saleOrderId; }
    public Integer getPoNumber() { return poNumber; }
    public String getPaymentDate() { return paymentDate; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getDirection() { return direction; }
}