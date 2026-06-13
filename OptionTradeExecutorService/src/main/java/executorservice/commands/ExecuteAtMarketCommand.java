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
import executorservice.cache.OrderCache;
import executorservice.cache.OrderData;
import executorservice.model.OrderResponseDTO;
import org.apache.commons.lang3.StringUtils;



public class ExecuteAtMarketCommand implements ICommand<ClientOrder>{

    private KiteConnect kiteConnect;
    private ClientOrder clientOrder;
    private OrderCache orderCache;
    private String clientUserId;

    public ExecuteAtMarketCommand(ClientOrder clientOrder, KiteConnect kiteConnect,
                                  OrderCache orderCache, String clientUserId)
    {
        this.clientOrder = clientOrder;
        this.kiteConnect = kiteConnect;
        this.orderCache = orderCache;
        this.clientUserId = clientUserId;
    }

    @Override
    public ITradeExecutionResult execute() {
        try {
            OrderParams orderParams = new OrderParams();
            orderParams.quantity = clientOrder.getLots() * clientOrder.getLotSize();
            orderParams.transactionType = clientOrder.getTransactionType();
            orderParams.orderType = Constants.ORDER_TYPE_MARKET;
            orderParams.tradingsymbol = clientOrder.getTradingSymbol();
            orderParams.exchange = Constants.EXCHANGE_NFO;
            orderParams.validity = Constants.VALIDITY_DAY;
            orderParams.product = Constants.PRODUCT_MIS;
            orderParams.marketProtection = 2; // % protection (0–100)
            orderParams.tag = String.format("%s-%s",clientUserId,clientOrder.getMasterOrderId());
            System.out.println("Placing order to buy at market with client order :" + clientOrder.toString() + "and order parameter: "+ getOrderParamString(orderParams));

            OrderResponse order = kiteConnect.placeOrder(orderParams, Constants.VARIETY_REGULAR);
            orderCache.add(clientOrder.getMasterOrderId(), new OrderData(order.orderId, kiteConnect.getUserId()));
            System.out.println("Order successfully place  to buy at market with order id :" + order.orderId + "all order parameter: " + getExecutedOrderString(order));
            OrderResponseDTO orderResponseDTO = new OrderResponseDTO("NA", order.orderId);
            ITradeExecutionResult tradeExecutionResult = new TradeExecutionResult(orderResponseDTO, true, "", clientOrder, order.orderId);
            return tradeExecutionResult;
        } catch (Exception | KiteException ex) {

            String message = ex.getMessage();
            if(ex instanceof com.zerodhatech.kiteconnect.kitehttp.exceptions.InputException)
            {
                message = ((InputException)ex).message;
            }
            if(ex instanceof com.zerodhatech.kiteconnect.kitehttp.exceptions.GeneralException)
            {
                message = ((GeneralException)ex).message;
            }

            System.out.println("Exception while placing order to buy at market" + ex + "message:" + message );
            ITradeExecutionResult tradeExecutionResult = new TradeExecutionResult(null, false, "Exception while executing trade :" + ex + " error : " + message, clientOrder, "");
            return tradeExecutionResult;
        }
    }

    @Override
    public boolean canExecute() {
        if(!StringUtils.isEmpty(clientOrder.getTradingSymbol()) &&
                (Double.compare(clientOrder.getLots(),0)!=0) &&
                (Double.compare(clientOrder.getLotSize(),0)!=0))
        {
            return true;
        }
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

//    private String getExecutedOrderString(Order executedOrder)
//    {
//        StringBuilder executedOrderBuilder = new StringBuilder();
//        executedOrderBuilder.append( "Order [price :" + (!StringUtils.isEmpty(executedOrder.price) ? executedOrder.price : " "))
//                .append( ", ExchangeOrderId :" + (!StringUtils.isEmpty(executedOrder.exchangeOrderId) ? executedOrder.exchangeOrderId : " "))
//                .append( ", disclosed_quantity :" + (!StringUtils.isEmpty(executedOrder.disclosedQuantity) ? executedOrder.disclosedQuantity : " "))
//                .append( ", validity :" + (!StringUtils.isEmpty(executedOrder.validity) ? executedOrder.validity : " "))
//                .append( ", tradingsymbol :" + (!StringUtils.isEmpty(executedOrder.tradingSymbol) ? executedOrder.tradingSymbol : " "))
//                .append( ", variety :" + (!StringUtils.isEmpty(executedOrder.orderVariety) ? executedOrder.orderVariety : " "))
//                .append( ", user_id :" + (!StringUtils.isEmpty(executedOrder.userId) ? executedOrder.userId : " "))
//                .append( ", order_type :" + (!StringUtils.isEmpty(executedOrder.orderType) ? executedOrder.orderType : " "))
//                .append( ", trigger_price :" + (!StringUtils.isEmpty(executedOrder.triggerPrice) ? executedOrder.triggerPrice : " "))
//                .append( ", status_message :" + (!StringUtils.isEmpty(executedOrder.statusMessage) ? executedOrder.statusMessage : " "))
//                .append( ", price :" + (!StringUtils.isEmpty(executedOrder.price) ? executedOrder.price : " "))
//                .append( ", status :" + (!StringUtils.isEmpty(executedOrder.status) ? executedOrder.status : " "))
//                .append( ", product :" + (!StringUtils.isEmpty(executedOrder.product) ? executedOrder.product : " "))
//                .append( ", placed_by :" + (!StringUtils.isEmpty(executedOrder.accountId) ? executedOrder.accountId : " "))
//                .append( ", exchange :" + (!StringUtils.isEmpty(executedOrder.exchange) ? executedOrder.exchange : " "))
//                .append( ", order_id :" + (!StringUtils.isEmpty(executedOrder.orderId) ? executedOrder.orderId : " "))
//                .append( ", symbol :" + (!StringUtils.isEmpty(executedOrder.symbol) ? executedOrder.symbol : " "))
//                .append( ", pending_quantity :" + (!StringUtils.isEmpty(executedOrder.pendingQuantity) ? executedOrder.pendingQuantity : " "))
//                .append( ", order_timestamp :" + ((executedOrder.orderTimestamp!=null) ? executedOrder.orderTimestamp.toString() : " "))
//                .append( ", exchangeTimestamp :" + ((executedOrder.exchangeTimestamp!=null) ? executedOrder.exchangeTimestamp : " "))
//                .append( ", average_price :" + (!StringUtils.isEmpty(executedOrder.averagePrice) ? executedOrder.averagePrice : " "))
//                .append( ", transaction_type :" + (!StringUtils.isEmpty(executedOrder.transactionType) ? executedOrder.transactionType : " "))
//                .append( ", filled_quantity :" + (!StringUtils.isEmpty(executedOrder.filledQuantity) ? executedOrder.filledQuantity : " "))
//                .append( ", quantity :" + (!StringUtils.isEmpty(executedOrder.quantity) ? executedOrder.quantity : " "))
//                .append( ", parent_order_id :" + (!StringUtils.isEmpty(executedOrder.parentOrderId) ? executedOrder.parentOrderId : " "))
//                .append( ", tag :" + (!StringUtils.isEmpty(executedOrder.tag) ? executedOrder.tag : " ")).append("]");
//
//        return executedOrderBuilder.toString();
//    }

    private String getExecutedOrderString(OrderResponse orderResponse)
    {
        StringBuilder executedOrderBuilder = new StringBuilder();
        executedOrderBuilder.append( "Order [id :" + (!StringUtils.isEmpty(orderResponse.orderId) ? orderResponse.orderId : " "));
        return executedOrderBuilder.toString();
    }

    private boolean isOrderBooked()
    {
        return false;
    }
}
