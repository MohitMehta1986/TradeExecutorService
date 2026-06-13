package executorservice;

import awesome.code.base.properties.IPropertiesProvider;
import awesome.code.base.result.IResultTable;
import awesome.code.base.result.ResultColumnType;
import awesome.code.result.Context;
import awesome.code.result.CoreClientImpl;
import awesome.code.result.MapBaseResultMetadata;
import awesome.code.result.MapBaseResultTable;
import changedocument.ChangeDocumentResponse;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.zerodhatech.models.Order;
import executorservice.commands.*;
import executorservice.model.OrderResponseDTO;
import fetchrequest.ContextName;
import fetchrequest.FetchLatchOrderRecordRequest;
import optiontrading.common.changeDocuments.LatchOrderChangeDocument;

import optiontrading.common.changeDocuments.Status;
import publisher.IDataPublisher;
import publisher.OrderWriter;
import util.ChangeDocumentProvider;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ClientOrderExecutor implements Callable<IResultTable> {

    private final ICommand commandToExecute;
    private static final String TRADING_SYMBOL = "option.trading.symbol";
    private static final String ERROR_WARNING ="option.trading.error.warning";
    private static final String CLIENT_ID ="option.trading.client.id";
    private static final String EXTERNAL_EXECUTION_STATUS="option.trading.external.execution.status";
    private static final String EXECUTION_STATUS="option.trading.execution.status";
    private static final String ORDER_ID="option.trading.execution.order.id";
    private static final String TRADE_ORDER_ID="option.trading.execution.trade.order.id";
    private static final String USER_NAME="option.trading.execution.trade.user.name";

    private final ClientOrder clientOrder;
    private final boolean isMockTrade;
    private final IDataPublisher dataPublisher;
    private final ExecutorService executorService;
    private final CoreClientImpl coreClient;
    private final Gson gson;
    private final int distributingLatchPollingDelayTime;
    private int maxCacheRetryAttempts = 1;

    public ClientOrderExecutor(ICommand commandToExecute, ClientOrder clientOrder,
                               boolean isMockTrade, IDataPublisher<String> dataPublisher,
                               CoreClientImpl coreClient, IPropertiesProvider propertiesProvider)
    {
        this.commandToExecute = commandToExecute;
        this.clientOrder = clientOrder;
        this.isMockTrade = isMockTrade;
        this.dataPublisher = dataPublisher;
        ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("order-writer-%d").build();
        executorService = Executors.newFixedThreadPool(5, tf);
        this.coreClient = coreClient;
        this.gson = new Gson();
        distributingLatchPollingDelayTime = propertiesProvider.getIntegerProperty("distributed.latch.polling.delay.seconds",1);
        maxCacheRetryAttempts = propertiesProvider.getIntegerProperty("trading.number.retries", 5);
    }

    @Override
    public IResultTable call() {
        ITradeExecutionResult tradeExecutionResult;
        if (isMockTrade) {
            System.out.println("Executing mock trade with parameter" + clientOrder.toString());
            Order order = new Order();
            order.status = "MockOrder";
            order.orderId = String.valueOf(Instant.now().toEpochMilli()) + "MockOrderId";
            order.tradingSymbol = clientOrder.getTradingSymbol();
            order.quantity = String.valueOf(clientOrder.getLots() * clientOrder.getLotSize());
            order.price = String.valueOf(clientOrder.getLimitPrice());
            OrderResponseDTO orderResponseDTO = new OrderResponseDTO(order.status, order.orderId);
            tradeExecutionResult = new TradeExecutionResult(orderResponseDTO, true, "", clientOrder, order.orderId);
            System.out.println("Execution completed mock trade with parameter" + clientOrder.toString());
        } else if (!commandToExecute.canExecute()) {
            tradeExecutionResult = new TradeExecutionResult(null, false, "Validation failed", clientOrder, "");
        } else {
            tradeExecutionResult = commandToExecute.execute();
        }

        if (tradeExecutionResult.isExecuted()) {
//            Future<ChangeDocumentResponse> changeDocumentResponseFuture = executorService.submit(new OrderWriter(coreClient, ChangeDocumentProvider.getOrderChangeDocument(tradeExecutionResult, commandToExecute, clientOrder)));
//            publishOrder(changeDocumentResponseFuture);
            //TODO:
//            publishLatchOrder(ChangeDocumentProvider.getLatchOrderChangeDocument(tradeExecutionResult, commandToExecute, clientOrder, isMockTrade));
//            boolean isOrderCompleted = isOrderCompleted(tradeExecutionResult.getTradeOrderId());
//            if (isOrderCompleted) {
//                System.out.println("Order is completed ");
//            } else {
//                System.out.println("Order is not completed after zerodha provided order id : " + tradeExecutionResult.getOrder().orderId);
//            }
        }
        return getResultTable(tradeExecutionResult);
    }

    private void publishOrder(Future<ChangeDocumentResponse> changeDocumentResponseFuture) {
        if(changeDocumentResponseFuture !=null)
        {
            try {
                ChangeDocumentResponse response =  changeDocumentResponseFuture.get(10, TimeUnit.SECONDS);
                System.out.println(response.toString());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.out.println("Error while publishing order" + e);
            }
        }
    }

    private void publishLatchOrder(LatchOrderChangeDocument latchOrderChangeDocument) {
        String computeRequest = gson.toJson(latchOrderChangeDocument, LatchOrderChangeDocument.class);
        ChangeDocumentResponse response = coreClient.publishChangeDocument(new Context(ContextName.OFFICIAL), "LatchOrderChangeDocument", computeRequest);
        if (response.getStatus().equals(Status.SUCCESS)) {
            System.out.println("Latch order record saved successfully " + latchOrderChangeDocument.toString());
        } else {
            System.out.println("Latch order record not saved " + latchOrderChangeDocument.toString());
        }
    }

    private IResultTable getResultTable(ITradeExecutionResult tradeExecutionResult)
    {
        MapBaseResultTable mapBaseResultTable = new MapBaseResultTable();
        MapBaseResultMetadata mapBaseResultMetadata = new MapBaseResultMetadata();
        mapBaseResultMetadata.setColumn(ResultColumnType.STRING, TRADING_SYMBOL);
        mapBaseResultMetadata.setColumn(ResultColumnType.STRING, ERROR_WARNING);
        mapBaseResultMetadata.setColumn(ResultColumnType.INT, CLIENT_ID);
        mapBaseResultMetadata.setColumn(ResultColumnType.STRING, EXECUTION_STATUS);
        mapBaseResultMetadata.setColumn(ResultColumnType.STRING, EXTERNAL_EXECUTION_STATUS);
        mapBaseResultMetadata.setColumn(ResultColumnType.STRING, ORDER_ID);
        mapBaseResultMetadata.setColumn(ResultColumnType.STRING, TRADE_ORDER_ID);
        mapBaseResultMetadata.setColumn(ResultColumnType.STRING, USER_NAME);
        Map<String, Object> rowMap = new HashMap<>();
        if(tradeExecutionResult.getOrder()!=null)
        {
            rowMap.put(EXECUTION_STATUS, "PASSED");
            rowMap.put(EXTERNAL_EXECUTION_STATUS, "NA");
            rowMap.put(ORDER_ID, tradeExecutionResult.getOrder().getOrderId());
            rowMap.put(TRADE_ORDER_ID, tradeExecutionResult.getClientOrder().getMasterOrderId());
        }else
        {
            rowMap.put(EXECUTION_STATUS, "FAILED");
            rowMap.put(TRADE_ORDER_ID, tradeExecutionResult.getClientOrder().getMasterOrderId());
        }

        rowMap.put(TRADING_SYMBOL, tradeExecutionResult.getClientOrder().getTradingSymbol());
        rowMap.put(ERROR_WARNING, tradeExecutionResult.getFailureReason());
        rowMap.put(CLIENT_ID, tradeExecutionResult.getClientOrder().getClientId());
        rowMap.put(USER_NAME, tradeExecutionResult.getClientOrder().getUserName());
        mapBaseResultTable.setResultMetadata(mapBaseResultMetadata);
        mapBaseResultTable.addNextRowMap(rowMap);
        return mapBaseResultTable;
    }

    private boolean isOrderCompleted(String orderId) {
        int i = 1;
        boolean latchStatusChanged = false;
        do {
            try {
                TimeUnit.SECONDS.sleep(distributingLatchPollingDelayTime);
                latchStatusChanged = hasLatchStatusChanged(orderId);
            } catch (InterruptedException e) {
                System.out.println("Error: Polling thread interrupted"+ e);
            } catch (Exception ex) {
                System.out.println("Exception while polling " + ex);
                if (i == maxCacheRetryAttempts) {
                    return false;
                }
            }
            i++;
        } while (!latchStatusChanged && i < maxCacheRetryAttempts);
        return latchStatusChanged;
    }

    private boolean hasLatchStatusChanged(String orderId)
    {
        FetchLatchOrderRecordRequest.Builder fetchLatchOrderRecordRequestBuilder = new FetchLatchOrderRecordRequest.Builder(100);
        fetchLatchOrderRecordRequestBuilder.orderId(orderId);
        String requestOrderId = "";
        String orderStatus = "";

        try(IResultTable resultTable = coreClient.fetchLatchOrder(fetchLatchOrderRecordRequestBuilder.build()))
        {
            while (resultTable.next())
            {
                requestOrderId = resultTable.getString("ORDERID");
                orderStatus = resultTable.getString("OPERATION_STATUS");
            }
        } catch (Exception ex)
        {
            System.out.println("Exception while polling "+ ex);
        }
        return orderStatus.equals("COMPLETE") || orderStatus.equals("COMPLETED");
    }

}
