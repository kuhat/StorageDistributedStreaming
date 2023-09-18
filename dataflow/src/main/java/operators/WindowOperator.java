package operators;

import workers.DataTuple;

import java.util.ArrayList;
import java.util.List;

public class WindowOperator extends Operator {
    protected long windowSize; // Window size (could be time or count)
    //    protected long windowInterval; // Window interval (could be time or count).
    //    Used for sliding window. But this project currently supports tumbling window only.
    protected List<DataTuple> windowData = new ArrayList<>(); // Window buffer data

    public WindowOperator(long windowSize) {
        this.windowSize = windowSize;
//        this.windowInterval = windowInterval;
        setType(OperatorType.WINDOW);
    }

    protected List<DataTuple> processWindowData() {
        return null;
    }
}

