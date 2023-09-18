package workers;

import utils.NetworkAddress;
import backends.DAG.WorkerInfo;
import java.util.Set;

public interface StreamStateMigrationCallBack {
    void perform(WorkerInfo targetWorkerInfo, Set<String> keys);
}
