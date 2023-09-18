package utils;

import rpc.output.OutputClient;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class IterableMapCollection<T extends OutputClient> implements Iterable<T>{
    private Map<UUID, T> collection;

    public IterableMapCollection()
    {
        this.collection = new LinkedHashMap<>();
    }

    public void add(T client)
    {
        this.collection.put(client.getClientID(), client);
    }

    public T get(UUID id)
    {
        return this.collection.get(id);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.collection.values().iterator();
    }
}
