package executorservice.cache;

public interface ICache<K,V> {

    V getValue(K key);
    void add(K key, V value);
    boolean contains(K key);
    boolean isEmpty();

}
