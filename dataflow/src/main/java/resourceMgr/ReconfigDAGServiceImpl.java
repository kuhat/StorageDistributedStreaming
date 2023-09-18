package resourceMgr;

import io.grpc.stub.StreamObserver;
import workers.Acknowledgement;
import workers.ReconfigConfirmation;
import workers.ReconfigDAGGrpc;
import workers.ReconfigRequest;

import java.util.stream.Stream;

/**
 * @projectName: team-3b
 * @package: resourceMgr
 * @className: ReconfigDAGServiceImpl
 * @author: Danny
 * @description: TODO
 * @date: 2023/3/23 14:44
 * @version: 1.0
 */
public class ReconfigDAGServiceImpl extends ReconfigDAGGrpc.ReconfigDAGImplBase {

    // Return a StreamObserver to client，Server side returns a StreamObserver object as a response

}
