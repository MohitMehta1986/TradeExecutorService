package executorservice.commands;


import com.zerodhatech.kiteconnect.KiteConnect;
import executorservice.cache.OrderCache;

public interface IUpdateOrderCommandProvider {
    ICommand getCommand(ClientOrder clientOrder, KiteConnect kiteConnect, String clientUserId);
}
