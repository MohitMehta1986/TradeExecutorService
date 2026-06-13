package listener;

import awesome.code.result.Context;
import awesome.code.result.CoreClientImpl;
import changedocument.ChangeDocumentResponse;
import com.google.gson.Gson;
import com.zerodhatech.models.Order;
import com.zerodhatech.ticker.OnOrderUpdate;
import fetchrequest.ContextName;
import optiontrading.common.changeDocuments.LatchOrderChangeDocument;
import optiontrading.common.changeDocuments.Operation;
import optiontrading.common.changeDocuments.OrderChangeDocument;
import optiontrading.common.changeDocuments.Status;
import org.apache.commons.lang3.StringUtils;
import util.LockUtil;
import util.ProcessResult;
import util.ProcessResultType;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderListener implements OnOrderUpdate {

    private final CoreClientImpl coreClient;
    private final Gson gson;
    private BlockingQueue<OrderEvent> orderQueue;
    private LockUtil lockUtil;
    public OrderListener(CoreClientImpl coreClient)
    {
        this.coreClient = coreClient;
        this.gson = new Gson();
        int queueSize = 1000;
        orderQueue = new ArrayBlockingQueue<>(queueSize);
        lockUtil = new LockUtil();
        startQueueConsumer();
    }

    @Override
    public void onOrderUpdate(Order order) {
        System.out.println("Recived order update for orderid" + order.orderId + " order value is : " + getExecutedOrderString(order));
        OrderEvent event = new OrderEvent(order, 0, order.orderTimestamp.toInstant().toEpochMilli());
       // orderQueue.add(event);
    }

    private void startQueueConsumer()
    {
        int poolSize = 3;
        ExecutorService executorService = (poolSize>1)?Executors.newFixedThreadPool(poolSize):Executors.newSingleThreadExecutor();
        updateRealTimeUpdatesInParallel(executorService, poolSize);
    }

    private void updateRealTimeUpdatesInParallel(ExecutorService executorService, int eventPoolSize) {
        for (int i = 0; i < eventPoolSize; i++) {
            executorService.execute(() -> {
                System.out.println(String.format(" %s Starting consumer on queue", Thread.currentThread().getName()));
                while (true) {
                    try {
                        OrderEvent orderEvent = orderQueue.take();
                        String orderId = orderEvent.getOrder().orderId;
                        ProcessResult<Object> lockAndApplyResult = lockUtil.lockAndApply(orderId,
                                id -> {
                                    updateOrder(orderEvent.getOrder());
                                    //updateLatchOrder(orderEvent.getOrder());
                                }, orderEvent.getUpdateTimeStamp());
                        processLockAndApplyResult(lockAndApplyResult, orderEvent);
                    } catch (Exception ex) {
                        System.out.println("Error : Exception while updating order " + ex);
                    }
                }
            });
        }
    }

    private void processLockAndApplyResult(ProcessResult<Object> result, OrderEvent event)
    {
        if(!result.getProcessResultType().isSuccess())
        {
            System.out.println("Error in processing event" + result.getDescription()+ event);
            if(result.getProcessResultType() == ProcessResultType.LOCK_TIMEOUT && event.getRetryCount()<1)
            {
                System.out.println("Timeout while acquiring lock so trying again " +  event);
                event.setRetryCount(event.getRetryCount()+1);
                orderQueue.add(event);
            }
        }
    }
    private boolean updateOrder(Order order) {
        OrderChangeDocument orderChangeDocument = null;
        if (order.orderType.equals("LIMIT")) {
            orderChangeDocument = new OrderChangeDocument(Operation.UPDATE, "zerodhaorderlistener", "");
            orderChangeDocument.setOrderId(order.orderId);
            orderChangeDocument.setTradeOrderId(order.orderId);
            orderChangeDocument.setBuyPrice(order.averagePrice);
            orderChangeDocument.setWorkflowStatus(order.status);
            orderChangeDocument.setOptionSymbol(order.tradingSymbol);
            orderChangeDocument.setSource("zerodhaorderlistener");
        }else if (order.orderType.equals("MARKET") && order.transactionType.equals("BUY") && !order.status.equals("OPEN")) {
            orderChangeDocument = new OrderChangeDocument(Operation.UPDATE, "zerodhaorderlistener", "");
            orderChangeDocument.setOrderId(order.orderId);
            orderChangeDocument.setTradeOrderId(order.orderId);
            orderChangeDocument.setBuyPrice(order.averagePrice);
            orderChangeDocument.setWorkflowStatus(order.status);
            orderChangeDocument.setOptionSymbol(order.tradingSymbol);
            orderChangeDocument.setSource("zerodhaorderlistener");
        } else if (order.orderType.equals("MARKET") && order.transactionType.equals("SELL") && !order.status.equals("OPEN")) {
            orderChangeDocument = new OrderChangeDocument(Operation.UPDATE, "zerodhaorderlistener", "");
            orderChangeDocument.setOrderId(order.orderId);
            orderChangeDocument.setTradeOrderId(order.orderId);
            orderChangeDocument.setSellPrice(order.averagePrice);
            orderChangeDocument.setWorkflowStatus(order.status);
            orderChangeDocument.setSource("zerodhaorderlistener");
        }
        //TODO: Cancel condition??
        if (orderChangeDocument != null) {
            String computeRequest = gson.toJson(orderChangeDocument, OrderChangeDocument.class);
            ChangeDocumentResponse response = coreClient.publishChangeDocument(new Context(ContextName.OFFICIAL), "OrderChangeDocument", computeRequest);
            if (response.getStatus().equals(Status.SUCCESS)) {
                System.out.println("order saved successfully " + orderChangeDocument.toString());
                return true;

            } else {
                System.out.println("order not saved " + orderChangeDocument.toString());
                return false;
            }
        }
        return true;
    }

    private boolean updateLatchOrder(Order order) {
        if (order.orderType.equals("LIMIT"))
        {
            LatchOrderChangeDocument latchOrderChangeDocument = new LatchOrderChangeDocument(Operation.UPSERT, "zerodhaorderlistener", "");
            latchOrderChangeDocument.setOrderId(order.orderId);
            latchOrderChangeDocument.setTradeOrderId(order.orderId);
            latchOrderChangeDocument.setOrderOperation(order.orderType);
            latchOrderChangeDocument.setOperationStatus(order.status);
            String computeRequest = gson.toJson(latchOrderChangeDocument, LatchOrderChangeDocument.class);
            ChangeDocumentResponse response = coreClient.publishChangeDocument(new Context(ContextName.OFFICIAL), "LatchOrderChangeDocument", computeRequest);
            if (response.getStatus().equals(Status.SUCCESS)) {
                System.out.println("Latch order record saved successfully " + latchOrderChangeDocument.toString());
                return true;
            } else {
                System.out.println("Latch order record not saved " + latchOrderChangeDocument.toString());
                return false;
            }
        }else if ((order.transactionType.equals("BUY") && !order.status.equals("OPEN")) ||
                (order.transactionType.equals("SELL") && !order.status.equals("OPEN"))) {
            LatchOrderChangeDocument latchOrderChangeDocument = new LatchOrderChangeDocument(Operation.UPSERT, "zerodhaorderlistener", "");
            latchOrderChangeDocument.setOrderId(order.orderId);
            latchOrderChangeDocument.setTradeOrderId(order.orderId);
            latchOrderChangeDocument.setOrderOperation(order.orderType);
            latchOrderChangeDocument.setOperationStatus(order.status);
            String computeRequest = gson.toJson(latchOrderChangeDocument, LatchOrderChangeDocument.class);
            ChangeDocumentResponse response = coreClient.publishChangeDocument(new Context(ContextName.OFFICIAL), "LatchOrderChangeDocument", computeRequest);
            if (response.getStatus().equals(Status.SUCCESS)) {
                System.out.println("Latch order record saved successfully " + latchOrderChangeDocument.toString());
                return true;
            } else {
                System.out.println("Latch order record not saved " + latchOrderChangeDocument.toString());
                return false;
            }
        }
        return true;
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
