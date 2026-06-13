//package executorservice;
//
//import com.google.protobuf.InvalidProtocolBufferException;
//import com.google.protobuf.util.JsonFormat;
//import com.zerodhatech.kiteconnect.KiteConnect;
//import com.zerodhatech.kiteconnect.kitehttp.SessionExpiryHook;
//import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
//import com.zerodhatech.kiteconnect.utils.Constants;
//import com.zerodhatech.models.Order;
//import com.zerodhatech.models.OrderParams;
//import com.zerodhatech.models.Position;
//import com.zerodhatech.ticker.KiteTicker;
//import com.zerodhatech.ticker.OnOrderUpdate;
//import options.trading.execution.proto.ComputeDocumentOuterClass;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class OptionTradeExecutor {
//
//    public static void main(String[] args) throws InvalidProtocolBufferException {
//        Map<String,String> computeMap = new HashMap<>();
//        computeMap.put("TradeOperation", "buyatmarket");
//        computeMap.put("Quantity", "100");
//        computeMap.put("TradeSymbol", "Testymbol");
//
//        ComputeDocumentOuterClass.ComputeMapRequest.Builder computeBuilder= ComputeDocumentOuterClass.ComputeMapRequest.newBuilder();
//        computeBuilder.putComputeAttributes("TradeOperation", "buyatmarket");
//        computeBuilder.putComputeAttributes("Quantity", "100");
//        computeBuilder.putComputeAttributes("TradeSymbol", "TestSymbol");
//
//        ComputeDocumentOuterClass.ComputeDocument.Builder protoBuilder = ComputeDocumentOuterClass.ComputeDocument.newBuilder();
//        protoBuilder.setChangeUserId("Test");
//        protoBuilder.setComputeMapRequest(computeBuilder);
//
//        String message  = JsonFormat.printer().print(protoBuilder);
//
//
//
//        KiteTicker ticker = new KiteTicker("9LfLlzG1WgFNfSIBAvR3sor4e2MFZ6Bi", "duedua6hyu7ai4ib");
//        ticker.setOnOrderUpdateListener(new OnOrderUpdate() {
//            @Override
//            public void onOrderUpdate(Order order) {
//                System.out.println("order update "+order.orderId);
//            }
//        });
//
//        try {
//            KiteConnect kc = getKiteSDKForUserID();
//            Order order = placeLimitOrder(kc, 108, "NIFTY2332317300CE", 1);
//
//            List<Order> orders = kc.getOrderHistory(order.orderId);
//            for(int i = 0; i< orders.size(); i++){
//                System.out.println(orders.get(i).orderId+" "+orders.get(i).status);
//            }
//            System.out.println("list size is "+orders.size());
//            closeOrderAtMarket(kc, orders.get(0));
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (KiteException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//    public static Order placeLimitOrder(KiteConnect kiteConnect, double limitPrice, String tradingSymbol, int quantity) throws KiteException, IOException {
//
//        OrderParams orderParams = new OrderParams();
//        orderParams.quantity = quantity;
//        orderParams.tradingsymbol = tradingSymbol;
//        orderParams.price = limitPrice;
//
//        orderParams.orderType = Constants.ORDER_TYPE_LIMIT;
//        orderParams.product = Constants.PRODUCT_MIS;
//        orderParams.exchange = Constants.EXCHANGE_NFO;
//        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
//        orderParams.validity = Constants.VALIDITY_DAY;
//
//        //orderParams.triggerPrice = 0.0;
//        //orderParams.tag = "myTag"; //tag is optional and it cannot be more than 8 characters and only alphanumeric is allowed
//
//        Order order = kiteConnect.placeOrder(orderParams, Constants.VARIETY_REGULAR);
//        System.out.println(order.orderId);
//        return order;
//    }
//
//
//
//
//    public void placeMarketOrder(KiteConnect kiteConnect, String tradingSymbol, int quantity) throws KiteException, IOException {
//
//        OrderParams orderParams = new OrderParams();
//        orderParams.price = 0.0;
//        orderParams.quantity = quantity;
//        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
//        orderParams.orderType = Constants.ORDER_TYPE_MARKET;
//        orderParams.tradingsymbol = tradingSymbol;
//        orderParams.exchange = Constants.EXCHANGE_NFO;
//        orderParams.validity = Constants.VALIDITY_DAY;
//        Order order11 = kiteConnect.placeOrder(orderParams, Constants.VARIETY_CO);
//        System.out.println(order11.orderId);
//    }
//
//    public static void closeOrderAtMarket(KiteConnect kiteConnect, Order order) throws KiteException, IOException {
//
//        OrderParams orderParams = new OrderParams();
//        orderParams.tradingsymbol = order.tradingSymbol;
//        orderParams.quantity = Integer.parseInt(order.quantity);
//        orderParams.transactionType = Constants.TRANSACTION_TYPE_SELL; // reversing the position to sell for square off assuming we will only buy options
//        orderParams.orderType = Constants.ORDER_TYPE_MARKET;
//        orderParams.exchange = Constants.EXCHANGE_NFO;
//        orderParams.validity = Constants.VALIDITY_DAY;
//        orderParams.product = Constants.PRODUCT_MIS;
//        Order order11 = kiteConnect.placeOrder(orderParams, Constants.VARIETY_REGULAR);
//        System.out.println(order11.orderId);
//    }
//
//    /** Get orderbook.*/
//    public void getOrders(KiteConnect kiteConnect) throws KiteException, IOException {
//        // Get orders returns order model which will have list of orders inside, which can be accessed as follows,
//        List<Order> orders = kiteConnect.getOrders();
//        for(int i = 0; i< orders.size(); i++){
//            System.out.println(orders.get(i).tradingSymbol+" "+orders.get(i).orderId+" "+orders.get(i).parentOrderId+
//                    " "+orders.get(i).orderType+" "+orders.get(i).averagePrice+" "+orders.get(i).exchangeTimestamp);
//        }
//
//        System.out.println("list of orders size is "+orders.size());
//    }
//
//    /** Get order details*/
//    public void getOrder(KiteConnect kiteConnect, String orderId) throws KiteException, IOException {
//        List<Order> orders = kiteConnect.getOrderHistory(orderId);
//        for(int i = 0; i< orders.size(); i++){
//            System.out.println(orders.get(i).orderId+" "+orders.get(i).status);
//        }
//        System.out.println("list size is "+orders.size());
//    }
//
//    /** Modify order.*/
//    public void modifyOrder(KiteConnect kiteConnect, String orderID) throws KiteException, IOException {
//        // Order modify request will return order model which will contain only order_id.
//        OrderParams orderParams =  new OrderParams();
//        orderParams.quantity = 1;
//        orderParams.orderType = Constants.ORDER_TYPE_LIMIT;
//        orderParams.tradingsymbol = "";
//        orderParams.product = Constants.PRODUCT_CNC;
//        orderParams.exchange = Constants.EXCHANGE_NSE;
//        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
//        orderParams.validity = Constants.VALIDITY_DAY;
//        orderParams.price = 122.25;
//
//        Order order21 = kiteConnect.modifyOrder(orderID, orderParams, Constants.VARIETY_REGULAR);
//        System.out.println(order21.orderId);
//    }
//
//    /** Cancel an order*/
//    public void cancelOrder(KiteConnect kiteConnect, String orderID) throws KiteException, IOException {
//        // Order modify request will return order model which will contain only order_id.
//        // Cancel order will return order model which will only have orderId.
//        Order order2 = kiteConnect.cancelOrder(orderID, Constants.VARIETY_REGULAR);
//        System.out.println(order2.orderId);
//    }
//
//    public static KiteConnect getKiteSDKForUserID() throws IOException, KiteException {
//        KiteConnect kiteSdk = new KiteConnect("duedua6hyu7ai4ib");
//        kiteSdk.setUserId("CAV264");
//        kiteSdk.setAccessToken("FVQXolI1cJP2nULAfCMlM8Tdg6YsDhId");
//        kiteSdk.setPublicToken("13IGqvrncPPd3rIvbfsPGToEyMgqzBF7");
//        kiteSdk.setSessionExpiryHook(new SessionExpiryHook() {
//            @Override
//            public void sessionExpired() {
//                System.out.println("session expired");
//            }
//        });
//
//        return kiteSdk;
//    }
//
//    /** Get all positions.*/
//    public void getPositions(KiteConnect kiteConnect) throws KiteException, IOException {
//        // Get positions returns position model which contains list of positions.
//        Map<String, List<Position>> position = kiteConnect.getPositions();
//        System.out.println(position.get("net").size());
//        System.out.println(position.get("day").size());
//        //System.out.println(position.get("net").get(0).);
//    }
//
//
//
//    /** Converts position*/
//    public void converPosition(KiteConnect kiteConnect) throws KiteException, IOException {
//        //Modify product can be used to change MIS to NRML(CNC) or NRML(CNC) to MIS.
//        JSONObject jsonObject6 = kiteConnect.convertPosition("ASHOKLEY", Constants.EXCHANGE_NSE, Constants.TRANSACTION_TYPE_BUY, Constants.POSITION_DAY, Constants.PRODUCT_MIS, Constants.PRODUCT_CNC, 1);
//        System.out.println(jsonObject6);
//    }
//
//    public void onIndexUpdateListner()
//    {
//        //1. Check if Target of Index price has met for an order OR Stop loss has met
//        // if yes, send a sell option which was bought earlier  ?
//    }
//
//
//    public void onOptionUpdateListner()
//    {
//        //1. Check if Target of Option price has met for an order OR Stop loss has met
//        // if yes, send a sell option which was bought earlier  ?
//    }
//
//}
