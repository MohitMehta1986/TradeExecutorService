package executorservice.commands;

import awesome.code.result.CoreClientImpl;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.GeneralException;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.InputException;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;
import executorservice.ITradeExecutionResult;
import executorservice.TradeExecutionResult;
import executorservice.model.OrderResponseDTO;
import org.apache.commons.lang3.StringUtils;

public class ModifyTargetCommand implements ICommand<ClientOrder>{

    private final ClientOrder clientOrder;
    private final KiteConnect kiteConnect;
    private final String userId;

    public ModifyTargetCommand(ClientOrder clientOrder, KiteConnect kiteConnect, String userId)
    {
        this.clientOrder = clientOrder;
        this.kiteConnect = kiteConnect;
        this.userId = userId;
    }

    @Override
    public ITradeExecutionResult execute() {
        try {
            OrderParams orderParams = new OrderParams();
            orderParams.quantity = clientOrder.getLots()* clientOrder.getLotSize();
            orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
            orderParams.orderType = Constants.ORDER_TYPE_LIMIT;
            orderParams.tradingsymbol = clientOrder.getTradingSymbol();
            orderParams.exchange = Constants.EXCHANGE_NFO;
            orderParams.validity = Constants.VALIDITY_DAY;
            orderParams.product = Constants.PRODUCT_MIS;
            orderParams.price = clientOrder.getTargetPrice();
            Order order = kiteConnect.modifyOrder(clientOrder.getOrderId(), orderParams, Constants.VARIETY_REGULAR);
            OrderResponseDTO orderResponseDTO = new OrderResponseDTO(order.status, order.orderId);
            ITradeExecutionResult tradeExecutionResult = new TradeExecutionResult(orderResponseDTO, true, "", clientOrder, order.orderId);
            return tradeExecutionResult;
        }catch (Exception | KiteException ex) {
            String message = ex.getMessage();
            if(ex instanceof com.zerodhatech.kiteconnect.kitehttp.exceptions.InputException)
            {
                message = ((InputException)ex).message;
            }
            if(ex instanceof com.zerodhatech.kiteconnect.kitehttp.exceptions.GeneralException)
            {
                message = ((GeneralException)ex).message;
            }
            System.out.println("Exception while placing order to buy at market"+ex+ "message :"+ message);
            ITradeExecutionResult tradeExecutionResult = new TradeExecutionResult(null, false, "Exception while executing trade", clientOrder, "");
            return tradeExecutionResult;
        }
    }

    @Override
    public boolean canExecute() {
        if(StringUtils.isEmpty(this.clientOrder.getOrderId())) {
            System.out.println("order is is empty, cannot modify existing order without order id");
            return false;
        }

        if(this.clientOrder.getTargetPrice()==null && this.clientOrder.getTargetPrice()!=0) {
            System.out.println("order is is empty, cannot modify stop loss for existing order without stop loss");
            return false;
        }
        return true;
    }
}
