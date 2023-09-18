package workers.outstream;

import org.jetbrains.annotations.NotNull;
import utils.NetworkAddress;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * collection of output stream clients
 *
 */
public class OutputStreamClientCollection implements Iterable<OutputStreamClient>{
    private Map<UUID, OutputStreamClient> collection;

    public OutputStreamClientCollection()
    {
        this.collection = new LinkedHashMap<>();
    }

    public void add(OutputStreamClient client)
    {
        this.collection.put(client.getClientID(), client);
    }
    public UUID remove(NetworkAddress address)
    {
        for(OutputStreamClient client : this.collection.values())
        {
            if(client.getHostAddress().equals(address))
            {
                this.collection.remove(client.getClientID());
                return client.getClientID();
            }
        }
        return null;
    }

    public OutputStreamClient get(UUID id)
    {
        return this.collection.get(id);
    }

    @NotNull
    @Override
    public Iterator<OutputStreamClient> iterator() {
        return this.collection.values().iterator();
    }
}
