package workers.outstream;

import rpc.output.OperatorOutputClient;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import operators.OperatorType;
import utils.NetworkAddress;
import workers.*;
import workers.storage.IKVStorage;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Output Stream Client
 * implementation of output edge / stream pipe that sends data to down flow operator
 *
 */
public class OutputStreamClient extends OperatorOutputClient {
    private final int MAX_BATCH_SIZE = 150; // 50 , 150 for testing， 150 would take 90 seconds
    private final int timeoutMilliSec = 25; // 25 for testing
    private final DataStreamGrpc.DataStreamBlockingStub stub;
    private ConcurrentLinkedQueue<DataTuple> cache;
    private ConcurrentLinkedQueue<DataTuple> batchBuffer;
    private Timer timer;

    /**
     * Constructor
     *
     * @param hostId
     * @param type
     * @param hostAddress
     * @param targetAddress
     */
    public OutputStreamClient(UUID hostId,
                              UUID targetID,
                              OperatorType type,
                              NetworkAddress hostAddress,
                              NetworkAddress targetAddress) {
        super(type, hostId, targetID, hostAddress, targetAddress);
        setLogger(Logger.getLogger(this.getClass().getName()));
        ManagedChannel channel = Grpc.newChannelBuilder(getTargetAddress().toString(),
                        InsecureChannelCredentials.create()).build();

        this.stub = DataStreamGrpc.newBlockingStub(channel);
        this.cache = new ConcurrentLinkedQueue<>();
        this.batchBuffer = new ConcurrentLinkedQueue<>();
        getLogger().log(Level.INFO, ">>>> OUT  stream initiated with dest: " + getTargetAddress() + ".>>>>");

        // start tumbling window for data batching and sending down flow
        this.timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                fireDataBatchDownFlow();
            }
        };
        timer.schedule(task, 0, timeoutMilliSec);
    }

    /**
     * this function get called by Client manager when data tuple is
     * assigned to this client for sending downflow
     * Send data to down flow operator
     *
     * @param dataTuple
     */
    public void sendDataDownFlow(DataTuple dataTuple) {
        cache.add(dataTuple);
    }

    public void fireDataBatchDownFlow()
    {
        // specify the batch size
        // at source, the rate need to be higher
        int batchSize = getOperatorType().equals(OperatorType.SOURCE) ? MAX_BATCH_SIZE * 3 : MAX_BATCH_SIZE;

        if(cache.size() == 0) {
            return;
        }

        try {
            DataBatch dataBatch;
            synchronized (cache)
            {
                // assemble the data batch
                if(cache.size() <= batchSize)
                {
                    this.batchBuffer.addAll(cache);
                    cache.clear();
                }
                else
                {
//                    getLogger().log(Level.WARNING, "!----!" + getOperatorType() + "(" + getHostAddress() + ") cache size is greater than max batch size: " + cache.size() +"/" + batchSize);

                    for(int i = 0; i < batchSize; i++)
                    {
                        this.batchBuffer.add(cache.poll());
                    }
                }

                dataBatch = DataBatch.newBuilder().addAllData(batchBuffer).build();
                //TODO: uncomment this for debugging
//                getLogger().log(Level.INFO, "Data transfer from " + getOperatorType() + "(" + getHostAddress() + ") to " + getTargetAddress()
//                        + " batch size: " + batchBuffer.size());

//                for(DataTuple dataTuple : dataBatch.getDataList())
//                {
//                    getLogger().log(Level.INFO, dataTuple.getData() + " " +  dataTuple.getCount() + " " + dataTuple.getTimestamp());
//                }
                batchBuffer.clear();
            }
           Acknowledgement ack = stub.transferData(dataBatch);
        } catch (StatusRuntimeException e) {
            // Cancel RPC
            getLogger().log(Level.WARNING,"RPC failed: {0}" + e.getStatus());
        }
    }

    public void sendState(IKVStorage storage, Set<String> keys)
    {
        //
        Set<DataTuple> stateSet = new HashSet<>();
        Map<String, Integer> states = storage.getStorage();
        getLogger().log(Level.INFO, "~~~~ Total States : " + states.size());
        getLogger().log(Level.INFO, "~~~~ Filtering States that begin with: " + keys);
        HashSet<String> set = new HashSet<>();
        try {
            for (Map.Entry<String, Integer> entry : states.entrySet()) {
//            getLogger().log(Level.INFO, "~~~~ Picking States in set" + keys +", checking : " +
//                    entry.getKey() + " matching: " + entry.getKey().toLowerCase().charAt(0));
                String key = entry.getKey();
                Integer value = entry.getValue();
                String capital = String.valueOf(key.toLowerCase().charAt(0));
                set.add(capital);
                if (keys.contains(capital)) {
                    DataTuple dataTuple = DataTuple.newBuilder().setData(key).setCount(value).setTimestamp(0).build();
                    stateSet.add(dataTuple);
                }
            }
        }
        catch(ConcurrentModificationException e)
        {
            getLogger().log(Level.SEVERE, getOperatorType() + "(" +getHostAddress() +") ConcurrentModificationException :" + e.getStackTrace());
        }
        getLogger().log(Level.INFO, "~~~~ All existing states begin with: " + set);
        DataBatch dataBatch = DataBatch.newBuilder().addAllData(stateSet).build();
        getLogger().log(Level.INFO, "~~~~ State transferring from " +
                getOperatorType() + "(" + getHostAddress() + ") to " + getTargetAddress()
                + " batch size: " + dataBatch.getDataList().size());
        ReconfigConfirmation confirmation = stub.sendState(dataBatch);
    }

    @Override
    public void finalize() {
        timer.cancel();
        getLogger().log(Level.INFO, ">>>> OUT  stream SHUTDOWN. Dest: " + getTargetAddress() + ".>>>>");
    }
}
