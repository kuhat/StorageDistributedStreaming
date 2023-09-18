package workers;

public interface StreamMergeStateCallBack {
    void perform(DataBatch dataBatch);
}
