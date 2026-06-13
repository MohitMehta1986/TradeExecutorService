package listener;

import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.ticker.KiteTicker;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class KiteOrderSubscriber implements Runnable{

    private KiteTicker kiteTicker;
    private OrderListener orderListener;
    private CountDownLatch countDownLatch;
    public KiteOrderSubscriber(OrderListener orderListener, KiteTicker kiteTicker, CountDownLatch countDownLatch)
    {
        this.kiteTicker = kiteTicker;
        this.orderListener = orderListener;
        this.countDownLatch = countDownLatch;
    }

    private void subscribe() throws KiteException {
        try {

            kiteTicker.setOnOrderUpdateListener(orderListener);

            // Make sure this is called before calling connect.
            kiteTicker.setTryReconnection(true);
            //maximum retries and should be greater than 0
            kiteTicker.setMaximumRetries(10);
            //set maximum retry interval in seconds
            kiteTicker.setMaximumRetryInterval(30);

            /** connects to com.zerodhatech.com.zerodhatech.ticker server for getting live quotes*/
            kiteTicker.connect();


            /** You can check, if websocket connection is open or not using the following method.*/
            boolean isConnected = kiteTicker.isConnectionOpen();

            System.out.println("connected order litener on thread :"+ Thread.currentThread().getName());
            countDownLatch.await();


        } catch (KiteException | InterruptedException e) {
            System.out.println("Exception while subscribing" + e);
        }
        finally {
            System.out.println("disconnecting option order subscriber on thread :"+ Thread.currentThread().getName());
            kiteTicker.disconnect();
        }

    }

    @Override
    public void run() {
        try {
            this.subscribe();
        } catch (KiteException e) {
            System.out.println("disconnecting option order subscriber  on thread :"+ Thread.currentThread().getName());
        }
    }
}
