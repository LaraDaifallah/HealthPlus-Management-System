package application;

public class InventoryTransaction {

    private int transactionId;
    private int batchId;
    private int employeeId;
    private String txnType;
    private int quantity;
    private String txnDate;
    private int referenceId;

    public InventoryTransaction(int transactionId, int batchId, int employeeId,
                                String txnType, int quantity, String txnDate,
                                int referenceId) {
        this.transactionId = transactionId;
        this.batchId = batchId;
        this.employeeId = employeeId;
        this.txnType = txnType;
        this.quantity = quantity;
        this.txnDate = txnDate;
        this.referenceId = referenceId;
    }

    public InventoryTransaction(int batchId, int employeeId,
                                String txnType, int quantity,
                                int referenceId) {
        this.batchId = batchId;
        this.employeeId = employeeId;
        this.txnType = txnType;
        this.quantity = quantity;
        this.referenceId = referenceId;
    }

    public int getTransactionId() { return transactionId; }
    public int getBatchId() { return batchId; }
    public int getEmployeeId() { return employeeId; }
    public String getTxnType() { return txnType; }
    public int getQuantity() { return quantity; }
    public String getTxnDate() { return txnDate; }
    public int getReferenceId() { return referenceId; }
}