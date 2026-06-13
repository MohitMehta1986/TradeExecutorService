package executorservice.strategies;

import awesome.code.base.properties.IPropertiesProvider;
import awesome.code.base.service.exception.ServiceException;
import awesome.code.result.CoreClientImpl;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import executorservice.KiteConnectProvider;
import executorservice.cache.OrderCache;
import publisher.IDataPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StrategyProvider {

    private Map<String, IOrderStrategy> strategyMap;
    private OrderCache orderCache;
    private OrderStrategyExecutor orderStrategyExecutor;

    public StrategyProvider(IPropertiesProvider propertiesProvider, KiteConnectProvider kiteConnectProvider,
                            CoreClientImpl coreClient, IDataPublisher dataPublisher, OrderCache orderCache)  throws ServiceException {
        try {
            this.orderStrategyExecutor = new OrderStrategyExecutor(propertiesProvider, coreClient, dataPublisher, orderCache, kiteConnectProvider);
            strategyMap = new ConcurrentHashMap<>();
            strategyMap.put("NEWORDER", getNewOrderStrategy(propertiesProvider));
            strategyMap.put("UPDATEORDER", getUpdateOrderStrategy(propertiesProvider, kiteConnectProvider, coreClient, dataPublisher));
            this.orderCache = orderCache;
        } catch (IOException | KiteException ex) {
            throw new ServiceException("Error while intializing strategy provider", (Exception) ex);
        }
    }

    public IOrderStrategy getOrderStrategy(String orderType) {
        return strategyMap.get(orderType);
    }

    private IOrderStrategy getNewOrderStrategy(IPropertiesProvider propertiesProvider) {
        return new NewOrderStrategy(propertiesProvider,  orderStrategyExecutor);
    }

    private IOrderStrategy getUpdateOrderStrategy(IPropertiesProvider propertiesProvider, KiteConnectProvider kiteConnectProvider,
                                                  CoreClientImpl coreClient, IDataPublisher dataPublisher) {
        return new UpdateOrderStrategy(orderStrategyExecutor);
    }
}
