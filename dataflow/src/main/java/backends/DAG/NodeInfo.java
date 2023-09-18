package backends.DAG;

import operators.OperatorType;
import utils.NetworkAddress;

import java.util.*;

public class NodeInfo {
    private List<NodeInfo> upstream;
    private WorkerInfo workerInfo;
    private List<NodeInfo> downstream;

    public NodeInfo(OperatorType operatorType)
    {
        this.upstream = new LinkedList<>();
        this.workerInfo = new WorkerInfo(operatorType);
        this.downstream = new LinkedList<>();
    }
    public NodeInfo(OperatorType operatorType,
                    NetworkAddress address,
                    UUID id)
    {
        this.upstream = new LinkedList<>();
        this.workerInfo = new WorkerInfo(address, operatorType, id);
        this.downstream = new LinkedList<>();
    }
    public NodeInfo(OperatorType operatorType, List<NodeInfo> upstream, List<NodeInfo> downstream)
    {
        this(operatorType);
        this.upstream = upstream;
        this.downstream = downstream;
    }

    public NodeInfo(OperatorType operatorType,
                    NetworkAddress address,
                    UUID id,
                    List<NodeInfo> upstream,
                    List<NodeInfo> downstream)
    {
        this(operatorType, address, id);
        this.upstream = upstream;
        this.downstream = downstream;
    }

    public boolean isReady()
    {
        return workerInfo.isInfoFullFilled();
    }

    public void setAddress(NetworkAddress address)
    {
        this.workerInfo.setAddress(address);
    }

    public void addUpStreamNode(NodeInfo node)
    {
        this.upstream.add(node);
    }

    public void addDownStreamNode(NodeInfo node)
    {
        this.downstream.add(node);
    }

//    public int hashCode()
//    {
//        return upstream.hashCode() ^ downstream.hashCode() & workerInfo.hashCode();
//    }

    public boolean equals(Object anotherNodeInfo)
    {
        if(!(anotherNodeInfo instanceof NodeInfo))
        {
            return false;
        }
        return workerInfo.equals(((NodeInfo)anotherNodeInfo).getWorkerInfo());
    }

    public String toString()
    {
        return "WorkerInfo: " + workerInfo.toString() +
                " UpStream num: " + upstream.size() +
                " DownStream num: " + downstream.size();
    }
    // getters and setters
    public List<NodeInfo> getUpstream() {
        return upstream;
    }

    public void setUpstream(List<NodeInfo> upstream) {
        this.upstream = upstream;
    }

    public WorkerInfo getWorkerInfo() {
        return workerInfo;
    }

    public void setWorkerInfo(WorkerInfo workerInfo) {
        this.workerInfo = workerInfo;
    }

    public List<NodeInfo> getDownstream() {
        return downstream;
    }

    public void setDownstream(List<NodeInfo> downstream) {
        this.downstream = downstream;
    }
}
