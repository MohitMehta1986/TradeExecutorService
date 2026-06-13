package executorservice;

import java.util.HashMap;
import java.util.Map;

public enum TradeOperation {
    EXIT_AT_MARKET("exitatmarket"),
    BUY_AT_LIMIT("buyatlimit"),
    EXECUTE_AT_SL("orderbysl"),
    BUY_AT_MARKET("buyatmarket"),
    CANCEL("cancel"),
    MODIFY_STOP_LOSS("modifystoploss"),
    MODIFY_TARGET("modifytarget"),
    MODIFY_LIMIT_ORDER("modifylimitorder"),
    MODIFY_SL_ORDER("modifyslorder"),
    MODIFY_SL_ORDER_ON_MARKET("modifyslorderexecuteonmarket");

    private final String tradeOperation;
    private static final Map<String, TradeOperation> ALL_TRADE_OPERATION = new HashMap<>();

    static {
        for (TradeOperation operation : values())
        {
            ALL_TRADE_OPERATION.put(operation.tradeOperation, operation);
        }
    }

    TradeOperation(String tradeOperation)
    {
        this.tradeOperation = tradeOperation;
    }

    public String getTradeOperation()
    {
        return this.tradeOperation;
    }

    public static TradeOperation getTradeOperation(String tradeOperation)
    {
        return ALL_TRADE_OPERATION.get(tradeOperation);
    }

}
