package workers.storage;

/**
 * abstract class for key-value storage
 * @param <K>
 * @param <V>
 */
public abstract class AbsKVStorage<K, V> extends AbsStorage implements IKVStorage<K, V>{
    public abstract void put(K key, V value);

    @Override
    public abstract V get(K key);

    @Override
    public abstract boolean containsKey(K key);
}
