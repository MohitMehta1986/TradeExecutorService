package executorservice.cache;

public class OrderData {
    private String orderId;
    private String accountId;

    public OrderData() {}

    public OrderData(String orderId, String accountId) {
        this.orderId = orderId;
        this.accountId = accountId;
    }

    public String getOrderId()   { return orderId; }
    public String getAccountId() { return accountId; }
}