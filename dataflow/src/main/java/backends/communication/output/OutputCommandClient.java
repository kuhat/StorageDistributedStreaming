package backends.communication.output;

import backends.DAG.NodeInfo;
import backends.DAG.WorkerInfo;
import rpc.output.OutputClient;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import utils.NetworkAddress;
import utils.ScaleDirection;
import workers.*;
import workers.router.RoutingTable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OutputCommandClient extends OutputClient {
    private final DataStreamGrpc.DataStreamBlockingStub stub;

    public OutputCommandClient(UUID hostId, UUID targetID, NetworkAddress hostAddress, NetworkAddress targetAddress) {
        super(hostId, targetID, hostAddress, targetAddress);
        setLogger(Logger.getLogger(this.getClass().getName()));
        ManagedChannel channel = Grpc.newChannelBuilder(getTargetAddress().toString(), InsecureChannelCredentials.create())
                .build();
        this.stub = DataStreamGrpc.newBlockingStub(channel);
        getLogger().log(Level.INFO, ">>>> Re-config Client(" + hostAddress +" )initiated with dest: " + targetAddress + ".>>>>");
    }

    public void sendStallPipelineCommand()
    {
        getLogger().log(Level.INFO, "~~~~ Sending stall pipeline command to " + getTargetAddress() + " ~~~~");
        ReconfigRequest request = ReconfigRequest.newBuilder().setId(getHostUUId().toString()).build();
        ReconfigConfirmation confirm = stub.stallPipeline(request);
    }

    public void sendDrainPipelineCommand()
    {
        getLogger().log(Level.INFO, "~~~~ Sending drain pipeline command to " + getTargetAddress() + " ~~~~");
        ReconfigRequest request = ReconfigRequest.newBuilder().setId(getHostUUId().toString()).build();
        ReconfigConfirmation confirm = stub.drainPipeline(request);
    }

    public void sendResumePipelineCommand()
    {
        getLogger().log(Level.INFO, "~~~~ Sending resume pipeline command to " + getTargetAddress() + " ~~~~");
        ReconfigRequest request = ReconfigRequest.newBuilder().setId(getHostUUId().toString()).build();
        ReconfigConfirmation confirm = stub.resumePipeline(request);
    }

    public void sendReconfigRouterCommand(RoutingTable table)
    {

    }

    public void sendReconfigRouterCommand(NodeInfo changedNode, ScaleDirection direction)
    {
        // TODO: CHECK HERE
        getLogger().log(Level.INFO, "~~~~ Sending reconfig router command to " + getTargetAddress() + " ~~~~");
        RouterReconfigRequest request = RouterReconfigRequest.newBuilder().setWorkerInfo(WorkerInformation.newBuilder()
                .setWorkerID(changedNode.getWorkerInfo().getId().toString())
                .setOperatorType(changedNode.getWorkerInfo().getOperatorType().toString())
                .setAddress(Address.newBuilder().
                        setDomain(changedNode.getWorkerInfo().getDomain()).
                        setPort(changedNode.getWorkerInfo().getPort())))
                .setDirection(workers.ScaleDirection.newBuilder().
                                setRequest(direction.equals(ScaleDirection.UP) ? 1 : -1)).build();
        ReconfigConfirmation confirm = stub.reconfigRouter(request);
    }

    public void sendStateMigrationCommand(WorkerInfo stateReceiverWorkerInfo, Set<Character> addressToKeyDiff)
    {
        getLogger().log(Level.INFO, "~~~~ Sending state migration command to " + getTargetAddress() + " ~~~~");

        AddressToKeyDiff.Builder addressToKeyDiffBuilder = AddressToKeyDiff.newBuilder();
        addressToKeyDiffBuilder.setWorkerInfo(WorkerInformation.newBuilder()
                .setAddress(Address.newBuilder().setDomain(stateReceiverWorkerInfo.getAddress().getDomain()).setPort(stateReceiverWorkerInfo.getAddress().getPort()))
                .setWorkerID(stateReceiverWorkerInfo.getId().toString())
                .setOperatorType(stateReceiverWorkerInfo.getOperatorType().toString()));
        for (Character c : addressToKeyDiff)
        {
            addressToKeyDiffBuilder.addKey(c.toString());
        }
        AddressToKeyDiff request = addressToKeyDiffBuilder.build();
        ReconfigConfirmation confirm = stub.stateMigration(request);
    }
}
