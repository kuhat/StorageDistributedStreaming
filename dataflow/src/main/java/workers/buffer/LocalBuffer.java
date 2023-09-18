package workers.buffer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * local buffer implementation
 *
 * @param <T>
 */
public class LocalBuffer<T> {
    private ConcurrentLinkedQueue<T> buffer;
    private long size = 0;
    public LocalBuffer()
    {
        this.buffer = new ConcurrentLinkedQueue<>();
        this.size = 0;
    }

    public void add(T dataTuple)
    {
        this.buffer.add(dataTuple);
        this.size++;
    }

    public void addAll(Collection<T> dataTuples)
    {
        this.buffer.addAll(dataTuples);
        this.size += dataTuples.size();
    }

    public void clear()
    {
        this.buffer.clear();
        this.size = 0;
    }

    public T getAndRemove()
    {
        if(this.size > 0)
        {
            this.size--;
        }
        return this.buffer.poll();
    }

    public Set<T> getAll()
    {
        return new HashSet<T>(this.buffer);
    }

    public long getSize()
    {
        return this.size;
    }
}
