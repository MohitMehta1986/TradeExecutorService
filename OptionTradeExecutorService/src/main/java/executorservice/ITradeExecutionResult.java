package executorservice;

import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderResponse;
import executorservice.commands.ClientOrder;
import executorservice.model.OrderResponseDTO;

public interface ITradeExecutionResult {

    OrderResponseDTO getOrder();
    boolean isExecuted();
    String getFailureReason();
    String getTradeOrderId();
    ClientOrder getClientOrder();
}
