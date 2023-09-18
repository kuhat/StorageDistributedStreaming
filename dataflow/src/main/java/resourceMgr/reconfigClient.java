package resourceMgr;

import rpc.output.OutputClient;
import io.grpc.*;
import utils.NetworkAddress;
import workers.*;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @projectName: team-3b
 * @package: resourceMgr
 * @className: reconfigClient
 * @author: Danny
 * @date: 2023/3/23 14:37
 * @version: 1.0
 */
public class reconfigClient extends OutputClient {
    private final ReconfigDAGGrpc.ReconfigDAGBlockingStub blockingStub;

    public reconfigClient(UUID hostId, UUID targetID, NetworkAddress hostAddress, NetworkAddress targetAddress) {
        super(hostId, targetID, hostAddress, targetAddress);
        ManagedChannel channel = Grpc.newChannelBuilder(getTargetAddress().toString(), InsecureChannelCredentials.create())
                .build();
        setLogger(Logger.getLogger(this.getClass().getName()));
        this.blockingStub = ReconfigDAGGrpc.newBlockingStub(channel);
    }

    // Perform reconfiguration operation by sending request to reconfig service GRPC api
    public String Reconfig(final String id) {
        ReconfigRequest reconfigRequest = ReconfigRequest.newBuilder().setId(id).build();
        ReconfigConfirmation confirmation;
        try {
            confirmation = blockingStub.reconfig(reconfigRequest);
            getLogger().log(Level.INFO, "request for reconfigure to address: " + getTargetAddress().toString());
            return confirmation.getConfirm();
        } catch (final StatusRuntimeException e) {
            return "Failed with " + e.getStatus().getCode().name();
        }
    }

}
