package executorservice.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OrderCache {

    private final ConcurrentMap<String, List<OrderData>> cache;
    private final OrderMappingStore orderMappingStore;

    public OrderCache(OrderMappingStore store) {
        this.orderMappingStore = store;
        this.cache = new ConcurrentHashMap<>();
        loadFromStoreOnStartup();
    }

    private void loadFromStoreOnStartup() {
        orderMappingStore.forEach(cache::put);
        System.out.println("OrderCache recovered " + cache.size() + " master orders");
    }

    public void add(String masterId, OrderData orderData) {
        List<OrderData> list = cache.computeIfAbsent(masterId, k -> new ArrayList<>());
        list.add(orderData);
        // persist at the index it was added
        orderMappingStore.putChild(masterId, list.size() - 1, orderData);
    }

    public void update(String masterId, int index, OrderData orderData) {
        List<OrderData> list = cache.get(masterId);
        if (list != null && index < list.size()) {
            list.set(index, orderData);
            orderMappingStore.putChild(masterId, index, orderData);
        }
    }

    public List<OrderData> get(String masterId) {
        List<OrderData> cached = cache.get(masterId);
        if (cached != null) return Collections.unmodifiableList(cached);

        // fallback to store
        List<OrderData> fromStore = orderMappingStore.getChildren(masterId);
        if (!fromStore.isEmpty()) cache.put(masterId, fromStore);
        return Collections.unmodifiableList(fromStore);
    }

    public void removeMaster(String masterId) {
        cache.remove(masterId);
        orderMappingStore.removeMaster(masterId);
    }
}