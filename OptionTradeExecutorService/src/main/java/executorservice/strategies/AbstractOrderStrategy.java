package executorservice.strategies;

import awesome.code.base.result.IResultTable;
import awesome.code.base.service.exception.ServiceException;
import executorservice.ExecutorParams;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static executorservice.Constants.*;
import static executorservice.Constants.TRIGGER_PRICE;

public abstract class AbstractOrderStrategy implements IOrderStrategy{

    public AbstractOrderStrategy()
    {
    }

    public abstract List<Future<IResultTable>> executeOrder(Map<String,String> computeAttributes) throws ServiceException;


    protected ExecutorParams.Builder getBuilder(Map<String, String> computeAttributes, boolean isMockTrade) throws ServiceException {
        String lots = computeAttributes.get(LOTS);
        String lotSize = computeAttributes.get(LOT_SIZE);
        String tradeSymbol = computeAttributes.get(TRADE_SYMBOL);
        String indexSymbol = computeAttributes.get(INDEX_SYMBOL);
        String transactionType = computeAttributes.get("TransactionType");
        if(StringUtils.isEmpty(lots) || StringUtils.isEmpty(tradeSymbol) || StringUtils.isEmpty(lotSize))
        {
            throw new ServiceException("Quantity or trade price is empty");
        }

        ExecutorParams.Builder executorParamBuilder = new ExecutorParams.Builder();
        executorParamBuilder.lots(Integer.parseInt(lots));
        executorParamBuilder.lotsSize(Integer.parseInt(lotSize));
        executorParamBuilder.tradingSymbol(tradeSymbol);
        executorParamBuilder.indexSymbol(indexSymbol);
        executorParamBuilder.isMockTrade(isMockTrade);
        executorParamBuilder.transactionType(transactionType);

        String clientOrderId = "";
        String tradeOrderId = "";
        if(computeAttributes.containsKey(CLIENT_ORDER_ID) && !StringUtils.isEmpty(computeAttributes.get(CLIENT_ORDER_ID)))
        {
            clientOrderId = computeAttributes.get(CLIENT_ORDER_ID);
            executorParamBuilder.orderId(clientOrderId);
        }
        if(computeAttributes.containsKey(TRADE_ORDER_ID) && !StringUtils.isEmpty(computeAttributes.get(TRADE_ORDER_ID)))
        {
            tradeOrderId = computeAttributes.get(TRADE_ORDER_ID);
            executorParamBuilder.tradeOrderId(tradeOrderId);
        }
        String clientOrderGenerator = "";
        if(computeAttributes.containsKey(REQUEST_USER))
        {
            clientOrderGenerator = computeAttributes.get(REQUEST_USER);
            executorParamBuilder.clientOrderGenerator(clientOrderGenerator);
        }

        if(computeAttributes.containsKey(TARGET_PRICE))
        {
            String targetPriceStr =  computeAttributes.get(TARGET_PRICE);
            double targetPrice = Double.parseDouble(targetPriceStr);
            executorParamBuilder.targetPrice(targetPrice);
        }

        if(computeAttributes.containsKey(TRIGGER_PRICE))
        {
            String triggerPriceStr =  computeAttributes.get(TRIGGER_PRICE);
            double triggerPrice = Double.parseDouble(triggerPriceStr);
            executorParamBuilder.triggerPrice(triggerPrice);
        }
        if(computeAttributes.containsKey(LIMIT_PRICE))
        {
            String limitPriceStr =  computeAttributes.get(LIMIT_PRICE);
            double limitPrice = Double.parseDouble(limitPriceStr);
            executorParamBuilder.limitPrice(limitPrice);
        }
        return executorParamBuilder;
    }
}

