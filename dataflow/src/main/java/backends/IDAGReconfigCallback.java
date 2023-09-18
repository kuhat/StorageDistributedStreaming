package backends;

import utils.ScaleDirection;

import java.util.List;
import java.util.UUID;

public interface IDAGReconfigCallback {
    void perform(int layerIndex, ScaleDirection scaleDirection);
}
