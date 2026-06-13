package executorservice.commands;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.GeneralException;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.InputException;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;
import executorservice.ITradeExecutionResult;
import executorservice.TradeExecutionResult;
import org.apache.commons.lang3.StringUtils;

public class ExitAtMarketCommand implements ICommand<ClientOrder>{

    private KiteConnect kiteConnect;
    private ClientOrder clientOrder;
    private String clientUserId;

    public ExitAtMarketCommand(ClientOrder clientOrder, KiteConnect kiteConnect, String clientUserId)
    {
        this.clientOrder = clientOrder;
        this.kiteConnect = kiteConnect;
        this.clientUserId = clientUserId;
    }

   //TODO: add locking
    @Override
    public ITradeExecutionResult execute() {
//        try {
//            OrderParams orderParams = new OrderParams();
//            orderParams.tradingsymbol = clientOrder.getTradingSymbol();
//            orderParams.quantity = clientOrder.getLots() * clientOrder.getLotSize();
//            orderParams.transactionType = clientOrder.getTransactionType(); // reversing the position to sell for square off assuming we will only buy options
//            orderParams.orderType = Constants.ORDER_TYPE_MARKET;
//            orderParams.exchange = Constants.EXCHANGE_NFO;
//            orderParams.validity = Constants.VALIDITY_DAY;
//            orderParams.product = Constants.PRODUCT_MIS;
//            //TODO order id?
//            //orderParams.parentOrderId
//            System.out.println("Exit order with client order :" + clientOrder.toString() + "and order parameter: "+ getOrderParamString(orderParams));
//            Order order = kiteConnect.placeOrder(orderParams, Constants.VARIETY_REGULAR);
//            System.out.println("Exit Order successfully place  with order id: " + order.orderId + "all order parameter: " + getExecutedOrderString(order));
//            ITradeExecutionResult tradeExecutionResult = new TradeExecutionResult(order, true, "", clientOrder, order.orderId);
//            return tradeExecutionResult;
//        }catch (Exception | KiteException ex)
//        {
//            String message = ex.getMessage();
//            if(ex instanceof com.zerodhatech.kiteconnect.kitehttp.exceptions.InputException)
//            {
//                message = ((InputException)ex).message;
//            }
//            if(ex instanceof com.zerodhatech.kiteconnect.kitehttp.exceptions.GeneralException)
//            {
//                message = ((GeneralException)ex).message;
//            }
//            System.out.println("Error when executing at market" + ex + "message: "+ message) ;
//            ITradeExecutionResult tradeExecutionResult = new TradeExecutionResult(null, false, message,clientOrder, "");
//            return tradeExecutionResult;
//        }

        System.out.println("Not implemented");
        return  null;
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    private boolean isOrderAlreadyExited()
    {
        return false;
    }

    private String getOrderParamString(OrderParams orderParams)
    {
        StringBuilder orderPramBuilder = new StringBuilder();
        orderPramBuilder.append("OrderParams [quantity :" + orderParams.quantity)
                .append(", transactionType :" +orderParams.transactionType)
                .append(", orderType :" + orderParams.orderType)
                .append(", tradingSymbol :" + orderParams.tradingsymbol)
                .append(", exchange :" + orderParams.exchange)
                .append(", validity :" + orderParams.validity)
                .append(", product :" + orderParams.product)
                .append("]");
        return orderPramBuilder.toString();
    }

}
