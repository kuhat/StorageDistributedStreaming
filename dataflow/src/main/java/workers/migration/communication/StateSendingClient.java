package workers.migration.communication;

import rpc.output.OperatorOutputClient;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import operators.OperatorType;
import utils.NetworkAddress;
import workers.Acknowledgement;
import workers.DataBatch;
import workers.DataStreamGrpc;
import workers.DataTuple;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StateSendingClient extends OperatorOutputClient {
    private final int MAX_BATCH_SIZE = 100;
    private final int timeoutMilliSec = 10;
    private final DataStreamGrpc.DataStreamBlockingStub stub;
    private LinkedList<DataTuple> cache;
    private Queue<DataTuple> batchBuffer;
    private Timer timer;

    public StateSendingClient(OperatorType type, UUID hostId, UUID targetID, NetworkAddress hostAddress, NetworkAddress targetAddress) {
        super(type, hostId,targetID, hostAddress, targetAddress);
        setLogger(Logger.getLogger(this.getClass().getName()));
        ManagedChannel channel = Grpc.newChannelBuilder(getTargetAddress().toString(), InsecureChannelCredentials.create())
                .build();
        this.stub = DataStreamGrpc.newBlockingStub(channel);
        this.cache = new LinkedList<>();
        this.batchBuffer = new LinkedList<>();
        getLogger().log(Level.INFO, "~~~~ " + getOperatorType() + "(" + getHostAddress() +  ") State Sending Server started ~~~~");
        this.timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                fireStateToDest();
            }
        };
        timer.schedule(task, 0, timeoutMilliSec);
    }
    public void sendState(DataTuple dataTuple)
    {
        cache.add(dataTuple);
    }
    public void fireStateToDest()
    {
        if(cache.size() == 0) {
            return;
        }

        try {
            DataBatch dataBatch;
            synchronized (cache)
            {
                // assemble the data batch
                if(cache.size() <= MAX_BATCH_SIZE)
                {
                    this.batchBuffer.addAll(cache);
                    cache.clear();
                }
                else
                {
                    for(int i = 0; i < MAX_BATCH_SIZE; i++)
                    {
                        this.batchBuffer.add(cache.poll());
                    }
                }

                dataBatch = DataBatch.newBuilder().addAllData(batchBuffer).build();
                getLogger().log(Level.INFO, "~~~~ State transfer from " + getOperatorType() + "(" + getHostAddress() + ") to " + getTargetAddress()
                        + " batch size: " + batchBuffer.size());
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

    @Override
    public void finalize() {
        timer.cancel();
    }
}
