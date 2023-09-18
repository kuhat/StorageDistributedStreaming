package workers.inputstream;
import backends.DAG.WorkerInfo;
import io.grpc.stub.StreamObserver;
import operators.OperatorType;
import utils.NetworkAddress;
import utils.ScaleDirection;
import workers.*;
import workers.buffer.LocalBuffer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Input Stream Service
 *
 * @author
 *
 */
public class InputStreamService extends DataStreamGrpc.DataStreamImplBase{
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private LocalBuffer<DataTuple> buffer;
    private InputStreamOnReceiveCallback onReceiveCallback;
    private StreamOperationSwitchCallBack streamOperationSwitchCallBack;
    private StreamRouterUpdateCallBack streamRouterUpdateCallBack;
    private StreamStateMigrationCallBack streamStateMigrationCallBack;
    private StreamMergeStateCallBack streamMergeStateCallBack;
    private StreamDrainCallBack streamDrainCallBack;


    /**
     * Constructor
     *
     * @param buffer
     */
    public InputStreamService(LocalBuffer buffer,
                              InputStreamOnReceiveCallback callback,
                              StreamOperationSwitchCallBack streamOperationSwitchCallBack,
                              StreamRouterUpdateCallBack streamRouterUpdateCallBack,
                              StreamStateMigrationCallBack streamStateMigrationCallBack,
                              StreamMergeStateCallBack streamMergeStateCallBack,
                              StreamDrainCallBack streamDrainCallBack)
    {
        this.buffer = buffer;
        this.onReceiveCallback = callback;
        this.streamOperationSwitchCallBack = streamOperationSwitchCallBack;
        this.streamRouterUpdateCallBack = streamRouterUpdateCallBack;
        this.streamStateMigrationCallBack = streamStateMigrationCallBack;
        this.streamMergeStateCallBack = streamMergeStateCallBack;
        this.streamDrainCallBack = streamDrainCallBack;
    }

    /**
     * grpc method implementation : transferData
     *
     * Transfer sendStatedata from client to server
     *
     * @param responseObserver
     * @return
     */
    @Override
    public void transferData(DataBatch dataBatch, StreamObserver<Acknowledgement> responseObserver)
    {
        // trigger callback
        this.onReceiveCallback.perform(dataBatch.getDataList().size());

        // add incoming data to input buffer
        buffer.addAll(dataBatch.getDataList());
        responseObserver.onNext(Acknowledgement.newBuilder().setConfirm(InputStreamService.class.getName() + "received all the data").build());
        responseObserver.onCompleted();
    }

    @Override
    public void stallPipeline(ReconfigRequest request, StreamObserver<ReconfigConfirmation> responseObserver) {
        logger.log(Level.INFO, "####Stalling pipeline####");
        streamOperationSwitchCallBack.perform();
        responseObserver.onNext(ReconfigConfirmation.newBuilder().setConfirm(InputStreamService.class.getName() + "Finished pipline stalling").build());
        responseObserver.onCompleted();
    }

    @Override
    public void resumePipeline(ReconfigRequest request, StreamObserver<ReconfigConfirmation> responseObserver) {
        logger.log(Level.INFO, "####Resuming pipeline####");
        streamOperationSwitchCallBack.perform();
        responseObserver.onNext(ReconfigConfirmation.newBuilder().setConfirm(InputStreamService.class.getName() + "resumed data flow").build());
        responseObserver.onCompleted();
    }

    @Override
    public void drainPipeline(ReconfigRequest request, StreamObserver<ReconfigConfirmation> responseObserver) {
        logger.log(Level.INFO, "####Draining pipeline####");
        streamDrainCallBack.perform();
        responseObserver.onNext(ReconfigConfirmation.newBuilder().setConfirm(InputStreamService.class.getName() + "Finished pipline draining").build());
        responseObserver.onCompleted();
    }

    @Override
    public void reconfigRouter(RouterReconfigRequest request, StreamObserver<ReconfigConfirmation> responseObserver) {
        logger.log(Level.INFO, "####Reconfiguring router####");
        OperatorType operatorType = OperatorType.toOperatorType(request.getWorkerInfo().getOperatorType());
        UUID id = UUID.fromString(request.getWorkerInfo().getWorkerID());
        NetworkAddress address = new NetworkAddress(request.getWorkerInfo().getAddress().getDomain(), request.getWorkerInfo().getAddress().getPort());
        ScaleDirection direction = ScaleDirection.toScaleDirection(request.getDirection().getRequest());
        streamRouterUpdateCallBack.perform(operatorType, id, address, direction);
        responseObserver.onNext(ReconfigConfirmation.newBuilder().setConfirm(InputStreamService.class.getName() + "received router reconfiguration").build());
        responseObserver.onCompleted();
    }
    @Override
    public void stateMigration(AddressToKeyDiff addressToKeyDiff, StreamObserver<ReconfigConfirmation> responseObserver)
    {
        logger.log(Level.INFO, "####State migration####");
        NetworkAddress address = new NetworkAddress(addressToKeyDiff.getWorkerInfo().getAddress().getDomain(), addressToKeyDiff.getWorkerInfo().getAddress().getPort());
        UUID id = UUID.fromString(addressToKeyDiff.getWorkerInfo().getWorkerID());
        OperatorType operatorType = OperatorType.toOperatorType(addressToKeyDiff.getWorkerInfo().getOperatorType());
        WorkerInfo targetWorkerInfo = new WorkerInfo(address, operatorType, id);
        Set<String> keys = new HashSet<>(addressToKeyDiff.getKeyList());

        streamStateMigrationCallBack.perform(targetWorkerInfo, keys);
        responseObserver.onNext(ReconfigConfirmation.newBuilder().setConfirm(InputStreamService.class.getName() + "received state migration").build());
        responseObserver.onCompleted();
    }

    @Override
    public void sendState(DataBatch dataBatch, StreamObserver<ReconfigConfirmation> responseObserver)
    {
        logger.log(Level.INFO, "####State Received cnt:" + dataBatch.getDataList().size() + "####");
        streamMergeStateCallBack.perform(dataBatch);
        responseObserver.onNext(ReconfigConfirmation.newBuilder().setConfirm(InputStreamService.class.getName() + "received states from sibling worker").build());
        responseObserver.onCompleted();
    }
}
