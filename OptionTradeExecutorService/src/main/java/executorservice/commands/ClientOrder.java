package executorservice.commands;

import java.util.Objects;

public class ClientOrder {

    private int clientId;
    private double limitPrice;
    private String tradingSymbol;
    private int lots;
    private String orderId;
    private Double stopLossPrice;
    private int lotSize;
    double defaultStopLoss;
    private String clientOrderOriginator;
    private String indexSymbol;
    private String tradeOrderId;
    private Double targetPrice;
    private String transactionType;
    private Double triggerPrice;
    private String masterOrderId;
    private String userName;

    private ClientOrder(ClientOrderBuilder clientOrderBuilder)
    {
        this.clientId = clientOrderBuilder.clientId;
        this.limitPrice = clientOrderBuilder.limitPrice;
        this.tradingSymbol = clientOrderBuilder.tradingSymbol;
        this.lots = clientOrderBuilder.lots;
        this.orderId = clientOrderBuilder.orderId;
        this.tradeOrderId = clientOrderBuilder.tradeOrderId;
        this.stopLossPrice = clientOrderBuilder.stopLossPrice;
        this.lotSize = clientOrderBuilder.lotSize;
        this.clientOrderOriginator = clientOrderBuilder.clientOrderGenerator;
        this.indexSymbol = clientOrderBuilder.indexSymbol;
        this.targetPrice = clientOrderBuilder.targetPrice;
        this.transactionType = clientOrderBuilder.transactionType;
        this.triggerPrice = clientOrderBuilder.triggerPrice;
        this.masterOrderId = clientOrderBuilder.masterOrderId;
        this.userName = clientOrderBuilder.userName;
    }

    public int getClientId() {
        return clientId;
    }

    public double getLimitPrice() {
        return limitPrice;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public int getLots() {
        return lots;
    }

    public String getOrderId() {
        return orderId;
    }

    public Double getStopLossPrice() {
        return stopLossPrice;
    }

    public int getLotSize() {
        return lotSize;
    }

    public String getClientOrderOriginator() {
        return clientOrderOriginator;
    }

    public String getIndexSymbol()
    {
        return indexSymbol;
    }

    public String getTradeOrderId() {
        return tradeOrderId;
    }

    public Double getTargetPrice()
    {
        return targetPrice;
    }

    public String getTransactionType()
    {
        return transactionType;
    }

    public Double getTriggerPrice() { return triggerPrice; }

    public String getMasterOrderId() { return masterOrderId; }

    public String getUserName() { return userName; }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientOrder{");
        sb.append("clientId=").append(clientId);
        sb.append(", limitPrice=").append(limitPrice);
        sb.append(", tradingSymbol='").append(tradingSymbol).append('\'');
        sb.append(", lots=").append(lots);
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", stopLossPrice=").append(stopLossPrice);
        sb.append(", lotSize=").append(lotSize);
        sb.append(", defaultStopLoss=").append(defaultStopLoss);
        sb.append(", clientOrderOriginator='").append(clientOrderOriginator).append('\'');
        sb.append(", indexSymbol='").append(indexSymbol).append('\'');
        sb.append(", tradeOrderId='").append(tradeOrderId).append('\'');
        sb.append(", targetPrice=").append(targetPrice).append('\'');
        sb.append(", transactionType=").append(transactionType).append('\'');
        sb.append(", triggerPrice=").append(triggerPrice);
        sb.append(", userName=").append(userName);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientOrder that = (ClientOrder) o;
        return clientId == that.clientId && Double.compare(that.limitPrice, limitPrice) == 0 && lots == that.lots && lotSize == that.lotSize && Objects.equals(tradingSymbol, that.tradingSymbol) && Objects.equals(orderId, that.orderId) && Objects.equals(stopLossPrice, that.stopLossPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, limitPrice, tradingSymbol, lots, orderId, stopLossPrice, lotSize);
    }

    public static class ClientOrderBuilder {
        private int clientId;
        private double limitPrice;
        private String tradingSymbol;
        private String orderId;
        private Double stopLossPrice;
        private int lots;
        private int lotSize;
        private String clientOrderGenerator;
        private String indexSymbol;
        private String tradeOrderId;
        private Double targetPrice;
        private String transactionType;
        private Double triggerPrice;
        private String masterOrderId;
        private String userName;

        public ClientOrderBuilder clientId(int clientId) {
            this.clientId = clientId;
            return this;
        }

        public ClientOrderBuilder userName(String userName)
        {
            this.userName = userName;
            return this;
        }

        public ClientOrderBuilder limitPrice(double limitPrice) {
            this.limitPrice = limitPrice;
            return this;
        }

        public ClientOrderBuilder tradingSymbol(String tradingSymbol) {
            this.tradingSymbol = tradingSymbol;
            return this;
        }

        public ClientOrderBuilder lots(int lots) {
            this.lots = lots;
            return this;
        }

        public ClientOrderBuilder lotSize(int lotSize) {
            this.lotSize = lotSize;
            return this;
        }

        public ClientOrderBuilder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public ClientOrderBuilder stopLossPrice(Double stopLossPrice) {
            this.stopLossPrice = stopLossPrice;
            return this;
        }

        public ClientOrderBuilder clientOrderGenerator(String clientOrderGenerator) {
            this.clientOrderGenerator = clientOrderGenerator;
            return this;
        }

        public ClientOrderBuilder indexSymbol(String indexSymbol) {
            this.indexSymbol = indexSymbol;
            return this;
        }

        public ClientOrderBuilder tradeOrderId(String tradeOrderId) {
            this.tradeOrderId = tradeOrderId;
            return this;
        }

        public ClientOrderBuilder targetPrice(Double targetPrice) {
            this.targetPrice = targetPrice;
            return this;
        }

        public ClientOrderBuilder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public ClientOrderBuilder triggerPrice(Double triggerPrice) {
            this.triggerPrice = triggerPrice;
            return this;
        }

        public ClientOrderBuilder masterOrderId(String masterOrderId)
        {
            this.masterOrderId = masterOrderId;
            return this;
        }

        public ClientOrder build() {
            return new ClientOrder(this);
        }
    }
}
