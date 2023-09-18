package workers.storage;

import java.util.HashMap;
import java.util.Map;

/**
 * key-value storage implementation over heap
 * @param <K>
 * @param <V>
 */
public class HeapKVStorage<K, V> extends AbsKVStorage<K,V>{
    private Map<K, V> storage;

    public HeapKVStorage()
    {
        this.storage = new HashMap<>();
    }
    @Override
    public void put(K key, V value) {
        this.getStorage().put(key, value);
    }

    @Override
    public V get(K key) {
        return this.getStorage().get(key);
    }

    @Override
    public boolean containsKey(K key) {
        return this.getStorage().containsKey(key);
    }

    public Map<K, V> getStorage()
    {
        return this.storage;
    }
}
