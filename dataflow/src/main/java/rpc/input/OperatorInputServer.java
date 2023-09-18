package rpc.input;

import operators.OperatorType;
import utils.NetworkAddress;

public abstract class OperatorInputServer extends InputServer{
    private OperatorType operatorType;

    public OperatorInputServer(OperatorType type, NetworkAddress serverAddress)
    {
        super(serverAddress);
        setOperatorType(type);
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(OperatorType operatorType) {
        this.operatorType = operatorType;
    }
}
