package workers.storage;

import kotlin.Pair;
import org.rocksdb.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RocksKVStorage<K, V> extends AbsKVStorage<K, V> {
    private RocksDB db; // local RocksDB instance

    public RocksKVStorage(String path) {
        try {
            Options options = new Options().setCreateIfMissing(true);
            db = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void put(K key, V value) {
        try {
            db.put(key.toString().getBytes(), value.toString().getBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public V get(K key) {
        try {
            return (V) new String(db.get(key.toString().getBytes()));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        try {
            return db.get(key.toString().getBytes()) != null;
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Map<K, V> getStorage() {
        //TODO: awaiting implementation
        return null;
    }

    public List<Pair<K, V>> getAll() {
        List<Pair<K, V>> list = new ArrayList<>();
        try (RocksIterator iter = db.newIterator()) {
            for (iter.seekToFirst(); iter.isValid(); iter.next()) {
                list.add(new Pair<>((K) new String(iter.key()), (V) new String(iter.value())));
            }
        }
        return list;
    }

    public boolean singleCopy(K key, RocksKVStorage<K, V> source) {
        try {
            byte[] value = source.db.get(key.toString().getBytes());
            if (value != null) {
                db.put(key.toString().getBytes(), value);
                return true;
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean rangeCopy(K start, K end, RocksKVStorage<K, V> source) {
        try {
            WriteBatch batch = new WriteBatch();
            RocksIterator iterator = source.db.newIterator();
            for (iterator.seek(start.toString().getBytes()); iterator.isValid() && new String(iterator.key()).compareTo(end.toString()) < 0; iterator.next()) {
                batch.put(iterator.key(), iterator.value());
            }
            source.db.write(new WriteOptions(), batch);
            return true;
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return false;
    }


//    public void clearAll(){
//        try{
//            byte[] start = null;
//            byte[] end = RocksDB.kMaxByteArray;
//        }
//    }

    public static void main(String[] args) {
//        RocksKVStorage<String, String> storage = new RocksKVStorage<String, String>("/tmp/rocksdb");
//        storage.put("key", "value");
//        System.out.println(storage.get("key"));
    }

}
