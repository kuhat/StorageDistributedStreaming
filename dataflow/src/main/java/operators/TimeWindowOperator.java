package operators;

import workers.DataTuple;
import workers.storage.IKVStorage;

import java.util.ArrayList;
import java.util.List;

public class TimeWindowOperator extends WindowOperator{
    public TimeWindowOperator(long windowSize) {
        super(windowSize);
    }


    private boolean process(DataTuple dataTuple) {
        long timestamp = dataTuple.getTimestamp();
        if (windowData.isEmpty() || timestamp - windowData.get(0).getTimestamp() <= windowSize) {
            windowData.add(dataTuple);
        } else {
            return true;
        }
        return false;
    }

    @Override
    public List<DataTuple> process(IKVStorage storage, List<DataTuple> dataTuples) {
        List<DataTuple> results = new ArrayList<>();
        for (DataTuple dataTuple : dataTuples) {
            boolean finishWindow = process(dataTuple);
            if (finishWindow) {
                results.addAll(processWindowData());
                windowData.clear();
                windowData.add(dataTuple);
            }
        }
        return results;
    }


}
