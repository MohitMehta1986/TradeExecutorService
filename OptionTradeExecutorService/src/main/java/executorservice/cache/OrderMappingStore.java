package executorservice.cache;

import net.openhft.chronicle.map.ChronicleMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class OrderMappingStore {

    private static final String FILE_NAME_PREFIX = "orderMasterSlave_shard_";
    private static final int SHARD_COUNT = 4; // must be power of 2
    private static final int SHARD_MASK  = SHARD_COUNT - 1;
    private static final int ENTRIES_PER_SHARD = 250_000 / SHARD_COUNT;

    private final ChronicleMap<String, String>[] shards;

    @SuppressWarnings("unchecked")
    public OrderMappingStore() {
        shards = new ChronicleMap[SHARD_COUNT];
        for (int i = 0; i < SHARD_COUNT; i++) {
            File file = new File(FILE_NAME_PREFIX + i + ".dat");
            try {
                shards[i] = ChronicleMap
                        .of(String.class, String.class)
                        .name("orderMasterOrderMap_shard_" + i)
                        .averageKey("MASTER_ORDER_ID_SAMPLE_12345:0")
                        .averageValue("ORDER_ID_SAMPLE_12345|ACCOUNT_ID_SAMPLE_123")
                        .entries(ENTRIES_PER_SHARD)
                        .createOrRecoverPersistedTo(file);
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize shard " + i, e);
            }
        }
        System.out.println("OrderMappingStore initialized with " + SHARD_COUNT + " shards");
    }

    // ------------------------------------------------------------------ shard routing

    private ChronicleMap<String, String> shardFor(String masterId) {
        // mask instead of % — avoids negative hashCode issues
        return shards[masterId.hashCode() & SHARD_MASK];
    }

    // ------------------------------------------------------------------ write

    public void putChild(String masterId, int index, OrderData data) {
        shardFor(masterId).put(compositeKey(masterId, index), serialize(data));
    }

    public void removeChild(String masterId, int index) {
        shardFor(masterId).remove(compositeKey(masterId, index));
    }

    public void removeMaster(String masterId) {
        shardFor(masterId).keySet()
                .removeIf(key -> masterIdFromKey(key).equals(masterId));
    }

    // ------------------------------------------------------------------ read

    public List<OrderData> getChildren(String masterId) {
        List<OrderData> result = new ArrayList<>();
        // only scan the one shard that owns this masterId
        shardFor(masterId).forEach((key, value) -> {
            if (masterIdFromKey(key).equals(masterId)) {
                result.add(deserialize(value));
            }
        });
        return result;
    }

    /** Startup recovery — iterate all shards */
    public void forEach(BiConsumer<String, List<OrderData>> consumer) {
        Map<String, List<OrderData>> grouped = new LinkedHashMap<>();
        for (ChronicleMap<String, String> shard : shards) {
            shard.forEach((key, value) ->
                    grouped.computeIfAbsent(masterIdFromKey(key), k -> new ArrayList<>())
                            .add(deserialize(value))
            );
        }
        grouped.forEach(consumer);
    }

    public void close() {
        for (ChronicleMap<String, String> shard : shards) {
            shard.close();
        }
    }

    // ------------------------------------------------------------------ codec

    private static String compositeKey(String masterId, int index) {
        return masterId + ":" + index;
    }

    private static String masterIdFromKey(String key) {
        return key.substring(0, key.lastIndexOf(':'));
    }

    private static String serialize(OrderData data) {
        return data.getOrderId() + "|" + data.getAccountId();
    }

    private static OrderData deserialize(String value) {
        String[] parts = value.split("\\|", 2);
        return new OrderData(parts[0], parts[1]);
    }
}