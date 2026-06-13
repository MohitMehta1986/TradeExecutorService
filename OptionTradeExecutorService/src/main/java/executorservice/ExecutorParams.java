package executorservice;

public class ExecutorParams {


    private String tradingSymbol;
    private int lots;
    private int lotsSize;
    private double limitPrice;
    private boolean isLimit;
    private boolean isMockTrade;
    private String clientOrderGenerator;
    private String indexSymbol;
    private String tradeOrderId;
    private String orderId;
    private double stopLossPrice;
    private double targetPrice;
    private String transactionType;
    private Double triggerPrice;
    private boolean isSLOrder;
    private boolean isUpdateOrder;

    public ExecutorParams(Builder builder)
    {
        this.tradingSymbol = builder.tradingSymbol;
        this.lots = builder.lots;
        this.lotsSize = builder.lotsSize;
        this.limitPrice = builder.limitPrice;
        this.isLimit = builder.isLimit;
        this.isMockTrade = builder.isMockTrade;
        this.clientOrderGenerator = builder.clientOrderGenerator;
        this.indexSymbol = builder.indexSymbol;
        this.tradeOrderId = builder.tradeOrderId;
        this.orderId = builder.orderId;
        this.stopLossPrice = builder.stopLossPrice;
        this.targetPrice = builder.targetPrice;
        this.transactionType = builder.transactionType;
        this.triggerPrice = builder.triggerPrice;
        this.isSLOrder = builder.isSLOrder;
        this.isUpdateOrder = builder.isUpdateOrder;

    }

    public String getTradingSymbol()
    {
        return tradingSymbol;
    }

    public int getLots() {
        return lots;
    }

    public int getLotsSize()
    {
        return lotsSize;
    }

    public boolean isLimit() {
        return isLimit;
    }

    public boolean isSLOrder() {
        return isSLOrder;
    }

    public boolean isUpdateOrder()
    {
        return isUpdateOrder;
    }


    public String getClientOrderGenerator() {
        return clientOrderGenerator;
    }


    public boolean isMockTrade() {
        return isMockTrade;
    }

    public String getTradeOrderId() {
        return tradeOrderId;
    }

    public String getIndexSymbol() {
        return indexSymbol;
    }

    public double getLimitPrice() {
        return limitPrice;
    }

    public String getOrderId()
    {
        return orderId;
    }

    public double getStopLossPrice() {
        return stopLossPrice;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public String getTransactionType(){
        return transactionType;
    }

    public Double getTriggerPrice()
    {
        return triggerPrice;
    }

    public static class Builder
    {
        private String tradingSymbol;
        private int lots;
        private int lotsSize;
        private double limitPrice;
        private boolean isLimit;
        private boolean isMockTrade;
        private String clientOrderGenerator;
        private String indexSymbol;
        private String tradeOrderId;
        private boolean isStopLossRequired;
        private double defaultStopLoss;
        private String orderId;
        private double stopLossPrice;
        private double targetPrice;
        private String transactionType;
        private Double triggerPrice;
        private boolean isSLOrder;
        private boolean isUpdateOrder;

        public Builder tradingSymbol(String tradingSymbol)
        {
            this.tradingSymbol = tradingSymbol;
            return this;
        }

        public Builder lots(int lots)
        {
            this.lots = lots;
            return this;
        }

        public Builder lotsSize(int lotSize)
        {
            this.lotsSize = lotSize;
            return this;
        }

        public Builder limitPrice(double limitPrice)
        {
            this.limitPrice = limitPrice;
            return this;
        }

        public Builder isLimit(boolean isLimit)
        {
            this.isLimit = isLimit;
            return this;
        }

        public Builder isMockTrade(boolean isMockTrade)
        {
            this.isMockTrade = isMockTrade;
            return this;
        }

        public Builder tradeOrderId(String tradeOrderId)
        {
            this.tradeOrderId = tradeOrderId;
            return this;
        }

        public Builder indexSymbol(String indexSymbol)
        {
            this.indexSymbol = indexSymbol;
            return this;
        }

        public Builder clientOrderGenerator(String clientOrderGenerator)
        {
            this.clientOrderGenerator = clientOrderGenerator;
            return this;
        }

        public Builder isStopLossRequired(boolean isStopLossRequired)
        {
            this.isStopLossRequired = isStopLossRequired;
            return this;
        }

        public Builder defaultStopLoss(double defaultStopLoss)
        {
            this.defaultStopLoss = defaultStopLoss;
            return this;
        }

        public Builder orderId(String orderId)
        {
            this.orderId = orderId;
            return this;
        }

        public Builder stopLoss(double stopLossPrice)
        {
            this.stopLossPrice = stopLossPrice;
            return this;
        }

        public Builder targetPrice(double targetPrice)
        {
            this.targetPrice = targetPrice;
            return this;
        }

        public Builder transactionType(String  transactionType)
        {
            this.transactionType = transactionType;
            return this;
        }

        public Builder triggerPrice(Double triggerPrice)
        {
            this.triggerPrice = triggerPrice;
            return this;
        }

        public Builder isSLOrder(boolean isSLOrder)
        {
            this.isSLOrder = isSLOrder;
            return this;
        }

        public Builder isUpdateOrder(boolean isUpdateOrder)
        {
            this.isUpdateOrder = isUpdateOrder;
            return this;
        }

        public ExecutorParams build()
        {
            return new ExecutorParams(this);
        }

    }
}
