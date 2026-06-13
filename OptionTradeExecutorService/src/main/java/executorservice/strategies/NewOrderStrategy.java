package executorservice.strategies;

import awesome.code.base.properties.IPropertiesProvider;
import awesome.code.base.result.IResultTable;
import awesome.code.base.service.exception.ServiceException;
import executorservice.*;
import executorservice.commands.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static executorservice.Constants.*;

public class NewOrderStrategy extends AbstractOrderStrategy {

    private final double defaultStopLoss;
    private final OrderStrategyExecutor orderStrategyExecutor;


    public NewOrderStrategy(IPropertiesProvider propertiesProvider, OrderStrategyExecutor orderStrategyExecutor) {
        defaultStopLoss = propertiesProvider.getDoubleProperty("option.trading.default.stop.loss", 1000.0);
        this.orderStrategyExecutor = orderStrategyExecutor;
    }

    @Override
    public List<Future<IResultTable>> executeOrder(Map<String, String> computeAttributes) throws ServiceException {
        boolean mockTrade = true;
        if (computeAttributes.containsKey(IS_MOCK_TRADE)) {
            mockTrade = Boolean.parseBoolean(computeAttributes.get(IS_MOCK_TRADE));
        }

        String tradeOperationStr = computeAttributes.get(TRADE_OPERATION);
        ExecutorParams.Builder executorParamBuilder = getBuilder(computeAttributes, mockTrade);
        TradeOperation tradeOperation = TradeOperation.getTradeOperation(tradeOperationStr);
        if (tradeOperation == null) {
            System.out.println("invalid Trade operation" + tradeOperationStr);
            throw new ServiceException("invalid Trade operation" + tradeOperationStr);
        }
        switch (tradeOperation) {
            case BUY_AT_LIMIT:
                return buyAtLimit(computeAttributes, executorParamBuilder);
            case BUY_AT_MARKET:
                return executeOrder(executorParamBuilder, ExecuteAtMarketCommand::new);
            case EXECUTE_AT_SL:
                String limitPrice = computeAttributes.get(LIMIT_PRICE);
                executorParamBuilder.limitPrice(Double.parseDouble(limitPrice));
                executorParamBuilder.isSLOrder(true);
                return executeOrder(executorParamBuilder, ExecuteAtSLCommand::new);
            default:
                throw new ServiceException("Trade operation not supported");
        }
    }

    private List<Future<IResultTable>> buyAtLimit(Map<String, String> computeAttributes,
                                                  ExecutorParams.Builder builder) throws ServiceException {
        String limitPrice = computeAttributes.get(LIMIT_PRICE);

        if (StringUtils.isEmpty(limitPrice)) {
            throw new ServiceException("limit price is required to buy at limit");
        }
        builder.limitPrice(Double.parseDouble(limitPrice));
        builder.isLimit(true);
        Double limitPriceDouble = Double.parseDouble(limitPrice);
        builder.limitPrice(limitPriceDouble);
        builder.defaultStopLoss(defaultStopLoss);
        return executeOrder(builder, ExecuteAtLimitCommand::new);
    }

    private List<Future<IResultTable>> executeOrder(ExecutorParams.Builder builder, INewOrderCommandProvider command) throws ServiceException {
        return orderStrategyExecutor.executeNewOrderStrategy(command, builder.build());
    }
}

