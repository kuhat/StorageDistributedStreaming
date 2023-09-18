package workers.migration.communication;

import io.grpc.stub.StreamObserver;
import workers.Acknowledgement;
import workers.DataBatch;
import workers.DataStreamGrpc;
import workers.inputstream.InputStreamOnReceiveCallback;
import workers.inputstream.InputStreamService;
import workers.storage.IKVStorage;

import java.util.logging.Logger;

public class StateReceivingService extends DataStreamGrpc.DataStreamImplBase{
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private IKVStorage storage;
    private InputStreamOnReceiveCallback onReceiveCallBack;

    public StateReceivingService(IKVStorage storage, InputStreamOnReceiveCallback onReceiveCallBack){
        this.storage = storage;
        this.onReceiveCallBack = onReceiveCallBack;
    }

    @Override
    public void transferData(DataBatch dataBatch, StreamObserver<Acknowledgement> responseObserver)
    {
        this.onReceiveCallBack.perform(dataBatch.getDataList().size());
        // TODO: implement Range Insert here
        //storage.rangeInsert(dataBatch.getDataList());
        responseObserver.onNext(Acknowledgement.newBuilder().setConfirm(InputStreamService.class.getName() + "received all the data").build());
        responseObserver.onCompleted();
    }

}
