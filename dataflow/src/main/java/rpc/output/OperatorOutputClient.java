package rpc.output;

import operators.OperatorType;
import utils.NetworkAddress;
import java.util.UUID;

public class OperatorOutputClient extends OutputClient{
    private OperatorType operatorType;
    public OperatorOutputClient(OperatorType type, UUID hostId, UUID targetID, NetworkAddress hostAddress, NetworkAddress targetAddress) {
        super(hostId, targetID, hostAddress, targetAddress);
        setOperatorType(type);
    }
    public OperatorType getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(OperatorType operatorType) {
        this.operatorType = operatorType;
    }
}
