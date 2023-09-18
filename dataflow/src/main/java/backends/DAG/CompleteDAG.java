package backends.DAG;

import backends.IDAGReconfigCallback;
import backends.communication.input.InputCommandServer;
import backends.communication.output.OutputCommandClient;
import backends.migration.MigrationManagerFactory;
import com.esotericsoftware.kryo.io.Output;
import operators.OperatorType;
import org.apache.flink.shaded.guava30.com.google.common.graph.Network;
import utils.MigrationMethod;
import utils.NetworkAddress;
import utils.OperationLayer;
import utils.ScaleDirection;

import java.util.*;

/**
 * Complete DAG
 * A complete DAG is a DAG that has all nodes connected
 */
public class CompleteDAG extends DAG{
//    private OutputCommandClient outputClient;
//    private InputCommandServer inputCommandServer;
    public CompleteDAG(List<OperationLayer> opLayers)
    {
        super(opLayers);
//        setInputCommandServer(new InputCommandServer(address, reconfigCallback));
//        setOutputClient(new OutputCommandClient(getId(), address, targetAddress));

        for(int currentlayerNum = opLayers.size() - 1; currentlayerNum >= 0; currentlayerNum--)
        {
            OperationLayer opLayer = opLayers.get(currentlayerNum);
            for(int currrentNodeNum = 0; currrentNodeNum < opLayer.getOperationCnt(); currrentNodeNum++) {
                // NodeInfo need to be filled in 1.NetworkAddress and 2.UUID later
                // by calling super.fillinWorkerInfo()
                this.addNode(opLayer.getType());
            }
        }
    }

    public NodeInfo addNode(OperatorType operatorType)
    {
        // instantiate a new node
        int layerIndex = getOperatorIndex(operatorType);
        NodeInfo newNode = new NodeInfo(operatorType);
        List<NodeInfo> adjList = new ArrayList<>();

        // connect upper stream nodes
        if(layerIndex - 1 >= 0)
        {
            for(NodeInfo node : getNodeLayers().get(layerIndex - 1))
            {
                newNode.addUpStreamNode(node);
                node.addDownStreamNode(newNode);
                getGraph().get(node).add(newNode);
            }
        }

        // connect lower stream nodes
        if(layerIndex + 1 < getNodeLayers().size())
        {
            for(NodeInfo node : getNodeLayers().get(layerIndex + 1))
            {
                newNode.addDownStreamNode(node);
                node.addUpStreamNode(newNode);
                adjList.add(node);
            }
        }

        // add to node layers
        getNodeLayers().get(layerIndex).add(newNode);

        // add to node collection
        getNodeCollection().add(newNode);

        // add to graph
        getGraph().put(newNode, adjList);
        return newNode;
    }

    public NodeInfo getLatestAddedNode(int layerIndex)
    {
        int lastAddedNodeIndex = getNodeLayers().get(layerIndex).size() - 1;
        return getNodeLayers().get(layerIndex).get(lastAddedNodeIndex);
    }

    public NodeInfo removeNode(int layerIndex)
    {
        int lastAddedNodeIndex = getNodeLayers().get(layerIndex).size() - 1;
        NodeInfo lastAddedNode = getNodeLayers().get(layerIndex).get(lastAddedNodeIndex);
        getNodeLayers().get(layerIndex).remove(lastAddedNodeIndex);
        getNodeCollection().remove(lastAddedNode);
        getGraph().remove(lastAddedNode);
        return lastAddedNode;
    }

    public void removeNode(NodeInfo nodeInfo)
    {
        for(List<NodeInfo> layer:getNodeLayers())
        {
            if(layer.contains(nodeInfo))
            {
                layer.remove(nodeInfo);
            }
        }
        getEntireLayer(nodeInfo).remove(nodeInfo);
        getNodeCollection().remove(nodeInfo);
        getGraph().remove(nodeInfo);
    }

    public List<NodeInfo> getEntireLayer(NodeInfo nodeInfo)
    {
        for(List<NodeInfo> layer:getNodeLayers())
        {
            if(layer.contains(nodeInfo))
            {
                return layer;
            }
        }
        return null;
    }



    //

    // getters and setters
//    public OutputCommandClient getOutputClient() {
//        return outputClient;
//    }
//
//    public void setOutputClient(OutputCommandClient outputClient) {
//        this.outputClient = outputClient;
//    }
//
//    public InputCommandServer getInputCommandServer() {
//        return inputCommandServer;
//    }
//
//    public void setInputCommandServer(InputCommandServer inputCommandServer) {
//        this.inputCommandServer = inputCommandServer;
//    }
}
