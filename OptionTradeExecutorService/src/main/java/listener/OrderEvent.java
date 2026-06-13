package listener;


import com.zerodhatech.models.Order;

public class OrderEvent {
    private int retryCount;
    private long updateTimeStamp;
    private Order order;

    public OrderEvent(Order order, int retryCount, long updateTimeStamp)
    {
        this.order = order;
        this.retryCount = retryCount;
        this.updateTimeStamp = updateTimeStamp;
    }

    public long getUpdateTimeStamp() {
        return updateTimeStamp;
    }

    public Order getOrder() {
        return order;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OrderEvent{");
        sb.append("retryCount=").append(retryCount);
        sb.append(", updateTimeStamp=").append(updateTimeStamp);
        sb.append(", order=").append(order);
        sb.append('}');
        return sb.toString();
    }
}
