package workers.storage;

import java.util.List;

//TODO: implement KeyRange class
public class KeyRange {
    private List<String> keys;

    public KeyRange(List<String> keys) {
        this.keys = keys;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public boolean contains(String key) {
        return keys.contains(key);
    }

    public void add(String key) {
        keys.add(key);
    }

    public void remove(String key) {
        keys.remove(key);
    }
}
