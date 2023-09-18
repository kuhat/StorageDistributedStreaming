package backends.DAG;

import backends.communication.output.OutputCommandClient;
import operators.OperatorType;
import utils.NetworkAddress;
import utils.OperationLayer;

import java.util.*;

public abstract class DAG {
    private Map<NodeInfo, List<NodeInfo>> graph; // adjacency list
    private List<List<NodeInfo>> nodeLayers; // node layers(upstream, workerinfo(address, operator type, uuid), downstream)
    private List<OperationLayer> operatorLayers; // describe each layer contains which operator, and operator count
    private Set<NodeInfo> nodeCollection; // all nodes
    private UUID id;

    public DAG(List<OperationLayer> operatorLayers)
    {
        setOperatorLayers(operatorLayers);
        setGraph(new HashMap<>());
        // fill in node layers slots
        List<List<NodeInfo>> layers = new ArrayList<>();
        for(int i = 0; i < operatorLayers.size(); i++)
        {
            layers.add(new ArrayList<>());
        }
        setNodeLayers(layers);
        setNodeCollection(new LinkedHashSet<>());
    }

    protected abstract NodeInfo addNode(OperatorType operatorType);
    protected abstract NodeInfo removeNode(int layerIndex);

    public NodeInfo addRunningWorkerToExecutionGraph(OperatorType operatorType, NetworkAddress address, UUID id, OutputCommandClient outputCommandClient)
    {
        NodeInfo availableNodeInfo = getNextAvailableSlot(operatorType);
        availableNodeInfo.getWorkerInfo().setAddress(address);
        availableNodeInfo.getWorkerInfo().setOutputCommandClient(outputCommandClient);
        availableNodeInfo.getWorkerInfo().setId(id);
        return availableNodeInfo;
    }

    public NodeInfo addRunningWorkerToExecutionGraph(WorkerInfo workerInfo)
    {
        NodeInfo availableNodeInfo = getNextAvailableSlot(workerInfo.getOperatorType());
        availableNodeInfo.getWorkerInfo().setAddress(workerInfo.getAddress());
        availableNodeInfo.getWorkerInfo().setOutputCommandClient(workerInfo.getOutputCommandClient());
        availableNodeInfo.getWorkerInfo().setId(workerInfo.getId());
        return availableNodeInfo;
    }


    public NodeInfo getNextAvailableSlot(OperatorType operatorType)
    {
        // locate operator layer index
        int layerIdx = 0;
        for(; layerIdx < getNodeLayers().size(); layerIdx++)
        {
            if(getNodeLayers().get(layerIdx).get(0).getWorkerInfo().getOperatorType().equals(operatorType))
            {
                break;
            }
        }

        // get a non-complete WorkerInfo instance
        for(NodeInfo nodeInfo : getNodeLayers().get(layerIdx))
        {
            if(!nodeInfo.isReady())
            {
                return nodeInfo;
            }
        }
        return null;
    }

    public int getOperatorIndex(OperatorType operatorType)
    {
        int layerIdx = 0;
        for(; layerIdx < getOperatorLayers().size(); layerIdx++)
        {
            if(getOperatorLayers().get(layerIdx).getType().equals(operatorType))
            {
                break;
            }
        }
        return layerIdx;
    }

    public List<NodeInfo> getNodeLayer(int layerIdx)
    {
        return getNodeLayers().get(layerIdx);
    }

    public List<NodeInfo> getNodeLayer(OperatorType operatorType)
    {
        int opLayerIndex =  getOperatorIndex(operatorType);
        return getNodeLayers().get(opLayerIndex);
    }

    //getters and setters
    public List<OperationLayer> getOperatorLayers() {
        return operatorLayers;
    }

    public void setOperatorLayers(List<OperationLayer> operatorLayers) {
        this.operatorLayers = operatorLayers;
    }
    protected Map<NodeInfo, List<NodeInfo>> getGraph() {
        return graph;
    }

    protected void setGraph(Map<NodeInfo, List<NodeInfo>> graph) {
        this.graph = graph;
    }

    protected List<List<NodeInfo>> getNodeLayers() {
        return nodeLayers;
    }

    protected void setNodeLayers(List<List<NodeInfo>> nodeLayers) {
        this.nodeLayers = nodeLayers;
    }

    public Set<NodeInfo> getNodeCollection() {
        return nodeCollection;
    }
    public void setNodeCollection(Set<NodeInfo> nodeCollection) {
        this.nodeCollection = nodeCollection;
    }
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
