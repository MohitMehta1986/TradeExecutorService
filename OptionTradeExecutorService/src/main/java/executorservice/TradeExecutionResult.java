package executorservice;

import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderResponse;
import executorservice.commands.ClientOrder;
import executorservice.model.OrderResponseDTO;

public class TradeExecutionResult implements ITradeExecutionResult{

    private OrderResponseDTO order;
    private boolean isExecuted;
    private String failureReason;
    private ClientOrder clientOrder;
    private String tradeOrderId;

    public TradeExecutionResult(OrderResponseDTO order, boolean isExecuted, String failureReason,
                                ClientOrder clientOrder, String tradeOrderId)
    {
        this.order = order;
        this.isExecuted = isExecuted;
        this.failureReason = failureReason;
        this.clientOrder = clientOrder;
        this.tradeOrderId = tradeOrderId;
    }
    @Override
    public OrderResponseDTO getOrder() {
        return order;
    }

    @Override
    public boolean isExecuted() {
        return isExecuted;
    }

    @Override
    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String getTradeOrderId() {
        return tradeOrderId;
    }

    @Override
    public ClientOrder getClientOrder() {
        return clientOrder;
    }

}
