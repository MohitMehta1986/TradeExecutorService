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

public class ModifySLPriceOrderCommand implements ICommand<ClientOrder>{

    private final ClientOrder clientOrder;
    private final KiteConnect kiteConnect;
    private final String userId;

    public ModifySLPriceOrderCommand(ClientOrder clientOrder, KiteConnect kiteConnect, String userId)
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
            orderParams.orderType = Constants.ORDER_TYPE_SL;
            orderParams.price = clientOrder.getLimitPrice();
            orderParams.triggerPrice = clientOrder.getTriggerPrice();
            System.out.println("Updating the price and trigger price for SL order :" + clientOrder.getOrderId()+ "and order parameter: "+ getOrderParamString(orderParams));
            Order order = kiteConnect.modifyOrder(clientOrder.getOrderId(), orderParams, Constants.VARIETY_REGULAR);
            System.out.println("Price updated successfully place  with order id: " + order.orderId + "all order parameter: " + getExecutedOrderString(order));
            OrderResponseDTO orderResponseDTO = new OrderResponseDTO(order.status, order.orderId);
            ITradeExecutionResult tradeExecutionResult = new TradeExecutionResult(orderResponseDTO, true, "", clientOrder, order.orderId);
            return tradeExecutionResult;
        }catch (Exception | KiteException ex) {
            String message = ex.getMessage();
            if(ex instanceof InputException)
            {
                message = ((InputException)ex).message;
            }
            if(ex instanceof GeneralException)
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
       if(StringUtils.isEmpty(this.clientOrder.getOrderId()))
       {
           System.out.println("order is is empty, cannot modify existing order without order id");
           return false;
       }
       return true;
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

    private String getExecutedOrderString(Order executedOrder)
    {
        StringBuilder executedOrderBuilder = new StringBuilder();
        executedOrderBuilder.append( "Order [price :" + (!StringUtils.isEmpty(executedOrder.price) ? executedOrder.price : " "))
                .append( ", ExchangeOrderId :" + (!StringUtils.isEmpty(executedOrder.exchangeOrderId) ? executedOrder.exchangeOrderId : " "))
                .append( ", disclosed_quantity :" + (!StringUtils.isEmpty(executedOrder.disclosedQuantity) ? executedOrder.disclosedQuantity : " "))
                .append( ", validity :" + (!StringUtils.isEmpty(executedOrder.validity) ? executedOrder.validity : " "))
                .append( ", tradingsymbol :" + (!StringUtils.isEmpty(executedOrder.tradingSymbol) ? executedOrder.tradingSymbol : " "))
                .append( ", variety :" + (!StringUtils.isEmpty(executedOrder.orderVariety) ? executedOrder.orderVariety : " "))
                .append( ", order_type :" + (!StringUtils.isEmpty(executedOrder.orderType) ? executedOrder.orderType : " "))
                .append( ", trigger_price :" + (!StringUtils.isEmpty(executedOrder.triggerPrice) ? executedOrder.triggerPrice : " "))
                .append( ", status_message :" + (!StringUtils.isEmpty(executedOrder.statusMessage) ? executedOrder.statusMessage : " "))
                .append( ", price :" + (!StringUtils.isEmpty(executedOrder.price) ? executedOrder.price : " "))
                .append( ", status :" + (!StringUtils.isEmpty(executedOrder.status) ? executedOrder.status : " "))
                .append( ", product :" + (!StringUtils.isEmpty(executedOrder.product) ? executedOrder.product : " "))
                .append( ", placed_by :" + (!StringUtils.isEmpty(executedOrder.accountId) ? executedOrder.accountId : " "))
                .append( ", exchange :" + (!StringUtils.isEmpty(executedOrder.exchange) ? executedOrder.exchange : " "))
                .append( ", order_id :" + (!StringUtils.isEmpty(executedOrder.orderId) ? executedOrder.orderId : " "))
                .append( ", pending_quantity :" + (!StringUtils.isEmpty(executedOrder.pendingQuantity) ? executedOrder.pendingQuantity : " "))
                .append( ", order_timestamp :" + ((executedOrder.orderTimestamp!=null) ? executedOrder.orderTimestamp.toString() : " "))
                .append( ", exchangeTimestamp :" + ((executedOrder.exchangeTimestamp!=null) ? executedOrder.exchangeTimestamp : " "))
                .append( ", average_price :" + (!StringUtils.isEmpty(executedOrder.averagePrice) ? executedOrder.averagePrice : " "))
                .append( ", transaction_type :" + (!StringUtils.isEmpty(executedOrder.transactionType) ? executedOrder.transactionType : " "))
                .append( ", filled_quantity :" + (!StringUtils.isEmpty(executedOrder.filledQuantity) ? executedOrder.filledQuantity : " "))
                .append( ", quantity :" + (!StringUtils.isEmpty(executedOrder.quantity) ? executedOrder.quantity : " "))
                .append( ", parent_order_id :" + (!StringUtils.isEmpty(executedOrder.parentOrderId) ? executedOrder.parentOrderId : " "))
                .append( ", tag :" + (!StringUtils.isEmpty(executedOrder.tag) ? executedOrder.tag : " ")).append("]");

        return executedOrderBuilder.toString();
    }
}
