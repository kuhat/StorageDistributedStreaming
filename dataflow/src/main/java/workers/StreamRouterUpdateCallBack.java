package workers;

import operators.OperatorType;
import utils.NetworkAddress;
import utils.ScaleDirection;

import java.util.UUID;

public interface StreamRouterUpdateCallBack {
    void perform(OperatorType operatorType, UUID id, NetworkAddress changedAddress, ScaleDirection scaleDirection);
}
