package executorservice.strategies;

import awesome.code.base.result.IResultTable;
import awesome.code.base.service.exception.ServiceException;
import executorservice.ExecutorParams;
import executorservice.TradeOperation;
import executorservice.commands.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static executorservice.Constants.*;

public class UpdateOrderStrategy extends AbstractOrderStrategy {


    private final OrderStrategyExecutor orderStrategyExecutor;

    public UpdateOrderStrategy(OrderStrategyExecutor orderStrategyExecutor) {

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
        executorParamBuilder.isUpdateOrder(true);
        switch (tradeOperation) {
            case EXIT_AT_MARKET:
                //return exitAtMarket(executorParamBuilder);
            case CANCEL:
                return executeOrder(executorParamBuilder, CancelOrderCommand::new);
            case MODIFY_TARGET:
                return executeOrder(executorParamBuilder, ModifyTargetCommand::new);
            case MODIFY_LIMIT_ORDER:
                return executeOrder(executorParamBuilder, ModifyLimitPriceOrderCommand::new);
            case MODIFY_SL_ORDER:
                return executeOrder(executorParamBuilder, ModifySLPriceOrderCommand::new);
            case MODIFY_SL_ORDER_ON_MARKET:
                return executeOrder(executorParamBuilder, ModifySLOrderToExecuteOnMarketCommand::new);
            default:
                throw new ServiceException("Trade operation not supported");
        }
    }

    private List<Future<IResultTable>> exitAtMarket(ExecutorParams.Builder builder) throws ServiceException {

        builder.limitPrice(0);
        return executeOrder(builder, ExitAtMarketCommand::new);
    }

    private List<Future<IResultTable>> executeOrder(ExecutorParams.Builder builder, IUpdateOrderCommandProvider command) throws ServiceException {
        return orderStrategyExecutor.executeUpdateOrderStrategy(command, builder.build());
    }
}
