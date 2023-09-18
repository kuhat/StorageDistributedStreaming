package workers.storage;

import java.util.Map;

/**
 * interface for key-value storage
 * @param <K>
 * @param <V>
 */
public interface IKVStorage<K, V> {

    public void put(K key, V value);
    public V get(K key);
    public boolean containsKey(K key);
    public Map<K,V> getStorage();
}
