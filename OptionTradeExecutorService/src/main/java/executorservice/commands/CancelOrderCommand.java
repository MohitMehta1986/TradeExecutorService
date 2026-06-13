package executorservice.commands;

import awesome.code.result.CoreClientImpl;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.GeneralException;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.InputException;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;
import com.zerodhatech.models.OrderResponse;
import executorservice.ITradeExecutionResult;
import executorservice.TradeExecutionResult;
import executorservice.cache.ICache;
import executorservice.cache.OrderData;
import executorservice.model.OrderResponseDTO;
import org.apache.commons.lang3.StringUtils;

public class CancelOrderCommand implements ICommand<ClientOrder>{

    private KiteConnect kiteConnect;
    private ClientOrder clientOrder;
    private String clientUserId;

    public CancelOrderCommand(ClientOrder clientOrder, KiteConnect kiteConnect, String clientUserId)
    {
        this.clientOrder = clientOrder;
        this.kiteConnect = kiteConnect;
        this.clientUserId = clientUserId;
    }

   //TODO: add locking
    @Override
    public ITradeExecutionResult execute() {
        try {
            System.out.println("cacelling order with parameter" + clientOrder.toString());
            Order cancelledOrder = kiteConnect.cancelOrder(clientOrder.getOrderId(), Constants.VARIETY_REGULAR);
            System.out.println("cancelled order:" + cancelledOrder.orderId);
            OrderResponseDTO orderResponseDTO = new OrderResponseDTO(cancelledOrder.status, cancelledOrder.orderId);
            ITradeExecutionResult tradeExecutionResult = new TradeExecutionResult(orderResponseDTO, true, "", clientOrder, clientOrder.getOrderId());
            //String cancelTrackerRequest = getTrackerRequest();
//            try {
//                coreClient.startTracker(cancelTrackerRequest);
//            } catch (Exception ex)
//            {
//                System.out.println("Error while cacelling all tracker all tracker");
//            }
            return tradeExecutionResult;
        }catch (Exception | KiteException ex)
        {
            System.out.println("Error while cancelling order:"+ex);
            String message = ex.getMessage();
            if(ex instanceof com.zerodhatech.kiteconnect.kitehttp.exceptions.InputException)
            {
                message = ((InputException)ex).message;
            }
            if(ex instanceof com.zerodhatech.kiteconnect.kitehttp.exceptions.GeneralException)
            {
                message = ((GeneralException)ex).message;
            }
            ITradeExecutionResult tradeExecutionResult = new TradeExecutionResult(null, false, "Exception while executing trade :"+ message,clientOrder, "");
            return tradeExecutionResult;
        }
    }

    @Override
    public boolean canExecute() {
        return !StringUtils.isEmpty(clientOrder.getOrderId());
    }

    private boolean isOrderExited()
    {
        return false;
    }

    private String getTrackerRequest()
    {
        String computeRequest="{\n" +
                "  \"computeMapRequest\": {\n" +
                "    \"computeAttributes\": {\n" +
                "      \"TrackerName\": \""+"cancelall"+"\",\n" +
                "      \"OptionSymbol\": \""+clientOrder.getTradingSymbol().trim()+"\",\n" +
                "      \"RequestUser\": \""+CancelOrderCommand.class.getSimpleName()+"\",\n" +
                "      \"IndexSymbol\": \""+clientOrder.getIndexSymbol().trim()+"\",\n" +
                "      \"OrderId\": \""+clientOrder.getOrderId().trim()+"\",\n" +
                "    }\n" +
                "  }\n" +
                "}";
        return computeRequest;
    }
}
