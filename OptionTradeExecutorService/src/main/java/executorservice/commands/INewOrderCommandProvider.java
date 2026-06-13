package executorservice.commands;

import com.zerodhatech.kiteconnect.KiteConnect;
import executorservice.cache.OrderCache;

public interface INewOrderCommandProvider {
    ICommand getCommand(ClientOrder clientOrder, KiteConnect kiteConnect, OrderCache orderCache, String clientUserId);
}
