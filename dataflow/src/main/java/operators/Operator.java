package operators;

import workers.DataTuple;
import workers.storage.IKVStorage;

import java.util.List;

public class Operator {
    // remember to set operator type
    private OperatorType type;

    // process single datatuple when get called each time
    // e.g.: Source operator will set datatuple word count:
    // dataTuple.toBuilder().setCount(10086).build();
    //
    // [IN] IKVStorage storage : worker storage
    // [IN & OUT]DataTuple dataTuple: data entity
    public DataTuple process(IKVStorage storage, DataTuple dataTuple) {
        return null;
    }

    public List<DataTuple> process(IKVStorage storage, List<DataTuple> dataTuples) {
        return null;
    }

    public OperatorType getType() {
        return type;
    }

    public void setType(OperatorType type) {
        this.type = type;
    }
}
