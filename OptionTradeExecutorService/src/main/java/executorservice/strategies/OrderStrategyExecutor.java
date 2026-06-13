package executorservice.strategies;

import awesome.code.base.properties.IPropertiesProvider;
import awesome.code.base.result.IResultTable;
import awesome.code.base.service.exception.ServiceException;
import awesome.code.result.CoreClientImpl;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import executorservice.ClientOrderExecutor;
import executorservice.ExecutorParams;
import executorservice.KiteConnectProvider;
import executorservice.cache.OrderCache;
import executorservice.cache.OrderData;
import executorservice.commands.ClientOrder;
import executorservice.commands.ICommand;
import executorservice.commands.INewOrderCommandProvider;
import executorservice.commands.IUpdateOrderCommandProvider;
import org.apache.commons.lang3.StringUtils;
import publisher.IDataPublisher;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

public class OrderStrategyExecutor {

    private final ExecutorService executorService;
    private final CoreClientImpl coreClient;
    private final IPropertiesProvider propertiesProvider;
    private final IDataPublisher dataPublisher;
    private final OrderCache orderCache;
    private final List<KiteConnect> kiteConnects;
    private final KiteConnectProvider kiteConnectProvider;

    public OrderStrategyExecutor(IPropertiesProvider propertiesProvider,
                                 CoreClientImpl coreClient, IDataPublisher dataPublisher,
                                 OrderCache orderCache,
                                 KiteConnectProvider kiteConnectProvider) throws IOException, KiteException {
        int numberOfThread = propertiesProvider.getIntegerProperty("option.trading.trade.executor.count", null);
        ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("trade-execution-runner-%d").build();
        executorService = Executors.newFixedThreadPool(numberOfThread, tf);
        this.coreClient = coreClient;
        this.dataPublisher = dataPublisher;
        this.propertiesProvider = propertiesProvider;
        this.orderCache = orderCache;
        this.kiteConnects = kiteConnectProvider.getKiteConnectForAllClint().entrySet().stream().map(item->item.getValue()).collect(Collectors.toList());
        this.kiteConnectProvider = kiteConnectProvider;
    }

    public List<Future<IResultTable>> executeNewOrderStrategy(INewOrderCommandProvider provider, ExecutorParams params) throws ServiceException {
        return executeOrderStrategy(params, (order, kite, userId) -> provider.getCommand(order, kite, orderCache, userId), false);
    }

    public List<Future<IResultTable>> executeUpdateOrderStrategy(IUpdateOrderCommandProvider provider, ExecutorParams params) throws ServiceException {
        return executeOrderStrategy(params, (order, kite, userId) -> provider.getCommand(order, kite, userId), true);
    }

    public List<Future<IResultTable>> executeOrderStrategy(ExecutorParams executorParams, OrderCommandFactory commandFactory, boolean isUpdate) throws ServiceException {
        List<Future<IResultTable>> futureResultList = new ArrayList<>();
        try {
            for (KiteConnect kiteConnect : kiteConnects) {
                Optional<String> resolvedOrderId = Optional.empty();

                if (isUpdate) {
                    resolvedOrderId = getOrderId(executorParams.getOrderId(), kiteConnect.getUserId());
                    if (!resolvedOrderId.isPresent()) {
                        System.out.println(String.format("Orderid is not present to update/cancel %s", executorParams.getOrderId()));
                        continue;
                    }
                }

               String userName = kiteConnectProvider.getUserName(kiteConnectProvider.clientId(kiteConnect.getUserId()));

                ClientOrder clientOrder = buildClientOrder(executorParams, resolvedOrderId.orElse(StringUtils.EMPTY),
                        executorParams.getOrderId(), userName);

                String userID = String.format("S%s", kiteConnectProvider.clientId(kiteConnect.getUserId()));
                ICommand command = commandFactory.create(clientOrder, kiteConnect, userID);
                Future<IResultTable> result = executorService.submit(
                        new ClientOrderExecutor(command, clientOrder, executorParams.isMockTrade(),
                                dataPublisher,
                                coreClient,
                                propertiesProvider
                        )
                );

                futureResultList.add(result);
            }
        } catch (Exception e) {
            throw new ServiceException("Error while executing trade", e);
        }
        return futureResultList;
    }

    private ClientOrder buildClientOrder(ExecutorParams params, String orderId, String masterOrderId, String userName) {

        ClientOrder.ClientOrderBuilder builder =
                new ClientOrder.ClientOrderBuilder()
                        .tradingSymbol(params.getTradingSymbol())
                        .clientOrderGenerator(params.getClientOrderGenerator())
                        .lots(params.getLots())
                        .lotSize(params.getLotsSize())
                        .stopLossPrice(params.getStopLossPrice())
                        .targetPrice(params.getTargetPrice())
                        .transactionType(params.getTransactionType())
                        .masterOrderId(masterOrderId)
                        .userName(userName)
                        .orderId(orderId);


        if (!StringUtils.isEmpty(params.getTradeOrderId())) {
            builder.tradeOrderId(params.getTradeOrderId());
        }

        if(params.isSLOrder() || params.isUpdateOrder())
        {
            builder.limitPrice(params.getLimitPrice());
            builder.triggerPrice(params.getTriggerPrice());
        }

        if (params.isLimit()) {
            builder.limitPrice(params.getLimitPrice());
        }

        return builder.build();
    }

    private Optional<String> getOrderId(String masterOrderId, String userId)
    {
        List<OrderData> orderDataList = orderCache.get(masterOrderId);
        List<OrderData> filterOrderData=  orderDataList.stream().filter(orderData -> orderData.getAccountId().equals(userId)).collect(Collectors.toList());
        if(filterOrderData.size()>1)
        {
            System.out.println(String.format("multiple order for master id %s and userid %s", masterOrderId, userId));
            return Optional.empty();
        }
        return Optional.of(filterOrderData.get(0).getOrderId());

    }


    @FunctionalInterface
    public interface OrderCommandFactory {
        ICommand create(ClientOrder order, KiteConnect kiteConnect, String userId);
    }

}
