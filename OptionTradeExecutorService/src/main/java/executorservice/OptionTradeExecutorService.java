package executorservice;

import awesome.code.base.properties.IPropertiesProvider;
import awesome.code.base.properties.IRequestContext;
import awesome.code.base.result.IResultTable;
import awesome.code.base.result.ResultException;
import awesome.code.base.service.AbstractService;
import awesome.code.base.service.IMessagePublisher;
import awesome.code.base.service.IMessageReader;
import awesome.code.base.service.exception.ServiceException;
import awesome.code.core.query.proto.QueryResponseOuterClass;
import awesome.code.exception.EndOfMessageException;
import awesome.code.result.CoreClientImpl;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import computedocument.OptionTradeComputeDocument;
import executorservice.cache.OrderCache;
import executorservice.cache.OrderMappingStore;
import executorservice.strategies.IOrderStrategy;
import executorservice.strategies.StrategyProvider;
import publisher.GRPCDataPublisher;
import publisher.IDataPublisher;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public class OptionTradeExecutorService extends AbstractService<OptionTradeComputeDocument, QueryResponseOuterClass.QueryResponse> {

    private static final String TRADE_OPERATION = "TradeOperation";
    private static final String IS_MOCK_TRADE = "IsMockTrade";

    private static KiteConnectProvider kiteConnectProvider;
    private static ExecutorService orderListener;
    private static IDataPublisher<String> dataPublisher;
    private static CoreClientImpl coreClient;
    private static StrategyProvider strategyProvider;

    @Override
    public void instanceInit(IPropertiesProvider iPropertiesProvider) {

    }

    public static void serviceInit(IPropertiesProvider propertiesProvider) throws ServiceException {
        kiteConnectProvider = new KiteConnectProvider(propertiesProvider);
        orderListener = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("trade-order-listener-%d").build());
        String projectId = propertiesProvider.getStringProperty("option.trading.project.id", null);
        String subscriptionId = propertiesProvider.getStringProperty("option.trading.subscription.id", "options-data-topic-sub");
        dataPublisher = new GRPCDataPublisher(subscriptionId, projectId);
        coreClient = new CoreClientImpl(propertiesProvider);
        OrderCache cache = new OrderCache(new OrderMappingStore());
        strategyProvider = new StrategyProvider(propertiesProvider, kiteConnectProvider, coreClient, dataPublisher, cache);
        //startOrderListener();

    }

    @Override
    public void processRequest(IRequestContext requestContext, IMessageReader<OptionTradeComputeDocument> requestQueue, IMessagePublisher<QueryResponseOuterClass.QueryResponse> responseQueue) throws ServiceException {
        try {
            while (requestQueue.next()) {
                OptionTradeComputeDocument optionTradeComputeDocument = requestQueue.read();
                Map<String, String> computeAttributes = optionTradeComputeDocument.getComputeAttributes();
                String tradeOperation = computeAttributes.get(TRADE_OPERATION);
                boolean mockTrade = true;
                if (computeAttributes.containsKey(IS_MOCK_TRADE)) {
                    mockTrade = Boolean.parseBoolean(computeAttributes.get(IS_MOCK_TRADE));
                }
                TradeOperation tradeOperation1 = TradeOperation.getTradeOperation(tradeOperation);
                if (tradeOperation1 == null) {
                    System.out.println("invalid Trade operation" + tradeOperation);
                    throw new ServiceException("invalid Trade operation" + tradeOperation);
                }

                String orderStrategy = getOrderStrategy(tradeOperation1);
                IOrderStrategy orderStrategyToExecute = strategyProvider.getOrderStrategy(orderStrategy);
                List<Future<IResultTable>> futuresResult = orderStrategyToExecute.executeOrder(computeAttributes);
                publishResult(responseQueue, futuresResult);
            }
        } catch (Exception ex) {
            System.out.println("Error while executing trade" + ex);
            throw new ServiceException(ex);
        } finally {
            try {
                responseQueue.setEndOfMessage();
            } catch (EndOfMessageException | InterruptedException e) {
                throw new ServiceException("Error while setting end of message exception", e);
            }
        }
    }

    private void publishResult(IMessagePublisher<QueryResponseOuterClass.QueryResponse> responseQueue, List<Future<IResultTable>> futureResultList) throws ServiceException {
        for (Future<IResultTable> result : futureResultList) {
            try {
                IResultTable resultTable = result.get();
                ResultPublisher.publishResult(resultTable, responseQueue);
            } catch (InterruptedException | ExecutionException | EndOfMessageException | ResultException ex) {
                System.out.println("error while execution" + ex);
                throw new ServiceException("error while execution" + ex);
            }
        }
    }

    private static void startOrderListener() {
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        KiteConnect kiteConnect = kiteConnectProvider.getKiteSDKForUserID("1");
//        KiteTicker kiteTicker = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
//        KiteOrderSubscriber kiteOrderSubscriber = new KiteOrderSubscriber(new OrderListener(coreClient),kiteTicker,countDownLatch);
//        orderListener.submit(kiteOrderSubscriber);
    }

    private String getOrderStrategy(TradeOperation tradeOperation) {
        if (tradeOperation.equals(TradeOperation.BUY_AT_LIMIT)
                || tradeOperation.equals(TradeOperation.EXECUTE_AT_SL)
                || tradeOperation.equals(TradeOperation.BUY_AT_MARKET)) {
            return "NEWORDER";
        } else {
            return "UPDATEORDER";
        }
    }
}
