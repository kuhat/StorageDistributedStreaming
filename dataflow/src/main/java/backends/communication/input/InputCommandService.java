package backends.communication.input;

import backends.IDAGReconfigCallback;
import io.grpc.stub.StreamObserver;
import utils.ScaleDirection;
import workers.ReconfigConfirmation;
import workers.ReconfigDAGGrpc;
import workers.ReconfigRequest;

import java.util.logging.Logger;

public class InputCommandService extends ReconfigDAGGrpc.ReconfigDAGImplBase {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private IDAGReconfigCallback reconfigCallback;

    public InputCommandService(IDAGReconfigCallback reconfigCallback) {
        this.reconfigCallback = reconfigCallback;
    }

    @Override
    public void reconfig(ReconfigRequest request, StreamObserver<ReconfigConfirmation> responseObserver)
    {
        // onNext: send data back to client
        responseObserver.onNext(reconfigDAG(request));
        responseObserver.onCompleted();
    }

    private ReconfigConfirmation reconfigDAG(ReconfigRequest request)
    {
        // get id from client
        String id = request.getId();
        System.out.println("received reconfig request by adding node to layer: " + id);
        ReconfigConfirmation confirmation = ReconfigConfirmation.newBuilder().setConfirm("Reconfig request received").build();
        this.reconfigCallback.perform(Integer.valueOf(request.getId()), ScaleDirection.UP);
        return confirmation;

    }
}
