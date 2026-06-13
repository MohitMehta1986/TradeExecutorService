package executorservice.commands;


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

public class ExecuteAtLimitCommand implements ICommand<ClientOrder>{

    private ClientOrder clientOrder;
    private KiteConnect kiteConnect;
    private OrderCache orderDataICache;
    private String clientUserId;

    public ExecuteAtLimitCommand(ClientOrder clientOrder, KiteConnect kiteConnect,
                                 OrderCache orderDataICache, String clientUserId)
    {
        this.kiteConnect = kiteConnect;
        this.clientOrder = clientOrder;
        this.orderDataICache = orderDataICache;
        this.clientUserId = clientUserId;
    }

    @Override
    public ITradeExecutionResult execute() {

        try {
            OrderParams orderParams = new OrderParams();
            orderParams.quantity = clientOrder.getLots() * clientOrder.getLotSize();
            orderParams.tradingsymbol = clientOrder.getTradingSymbol();
            orderParams.price = clientOrder.getLimitPrice();
            //orderParams.stoploss = Util.getDefaultStopLossPrice(clientOrder.getLimitPrice(), clientOrder.getStopLossPrice(), clientOrder.getLots(), clientOrder.getLotSize());
            orderParams.orderType = Constants.ORDER_TYPE_LIMIT;
            orderParams.product = Constants.PRODUCT_MIS;
            orderParams.exchange = Constants.EXCHANGE_NFO;
            orderParams.transactionType = clientOrder.getTransactionType();
            orderParams.validity = Constants.VALIDITY_DAY;
            orderParams.tag = String.format("%s-%s",clientUserId,clientOrder.getMasterOrderId());
            System.out.println("Placing order to buy at limit with client order :" + clientOrder.toString() + "and order parameter: "+ getOrderParamString(orderParams));
            OrderResponse order = kiteConnect.placeOrder(orderParams, Constants.VARIETY_REGULAR);
            OrderResponseDTO orderResponseDTO = new OrderResponseDTO("NA", order.orderId);

            orderDataICache.add(clientOrder.getMasterOrderId(), new OrderData(order.orderId, kiteConnect.getUserId()));
            System.out.println("Order successfully place  to buy at limit with order id: " + order.orderId + "all order parameter: " + getExecutedOrderString(order));
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
            System.out.println("Exception while placing order to buy at market"+ex+ "message :"+ message);
            ITradeExecutionResult tradeExecutionResult = new TradeExecutionResult(null, false, message, clientOrder, clientOrder.getMasterOrderId());
            return tradeExecutionResult;
        }
    }

    @Override
    public boolean canExecute() {
        if(!StringUtils.isEmpty(clientOrder.getTradingSymbol()) &&
                Double.compare(clientOrder.getLots(),0)!=0 &&
                Double.compare(clientOrder.getLotSize(),0)!=0 &&
                Double.compare(clientOrder.getLimitPrice(),0) !=0)
        {
            return true;
        }
        if(StringUtils.isEmpty(clientOrder.getOrderId()))
        {
            System.out.println("master prder id is missing");
            return false;

        }
        return false;
    }

    private boolean isOrderBooked()
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
                .append(", price :" + orderParams.price)
                .append("]");
        return orderPramBuilder.toString();
    }

    private String getExecutedOrderString(OrderResponse orderResponse)
    {
        StringBuilder executedOrderBuilder = new StringBuilder();
        executedOrderBuilder.append( "Order [id :" + (!StringUtils.isEmpty(orderResponse.orderId) ? orderResponse.orderId : " "));
        return executedOrderBuilder.toString();
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
}