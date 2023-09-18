package backends.migration.strategy;

import backends.DAG.NodeInfo;
import backends.engines.MultilayerDAGEngine;
import operators.OperatorType;
import utils.NetworkAddress;
import utils.ScaleDirection;
import workers.router.RoutingTable;
import workers.storage.KeyRange;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PipelineStallMigrationManager extends MigrationManager {

    public PipelineStallMigrationManager(MultilayerDAGEngine engine) {
        super(engine);
        setLogger(Logger.getLogger(this.getClass().getName()));
    }

    @Override
    public void performStateMigration(KeyRange keyRange, NetworkAddress destAddress){
        // migrate data
    }

    @Override
    public void performRouterChange(RoutingTable table) {
        // change router
    }

    // up scale by adding 1 new worker
    public void upScale(int layerIndex)
    {
        if(layerIndex != 1)
        {
            getLogger().log(Level.SEVERE, "Cannot add new node to layer index other than 1");
        }

        // update computing graph
        // add new node to the mimic router in engine
        OperatorType opType = OperatorType.COUNT;
        NodeInfo newNode = getEngine().connectAdditionalWorkerToGraph(opType);
        getLogger().log(Level.INFO, "DAGEngine Router after adding new client: " + getEngine().getSourceRouter().getCurrentRoutingPlan());

        // stop source pipeline
        for(NodeInfo nodeInfo : newNode.getUpstream())
        {
            getEngine().stallPipeline(nodeInfo);
        }
        // stop sink pipeline
//        for(NodeInfo nodeInfo : newNode.getDownstream())
//        {
//            getEngine().stallPipeline(nodeInfo);
//        }

        // change source router
        for(NodeInfo nodeInfo : newNode.getUpstream())
        {
            getEngine().updateNodeRouter(nodeInfo, newNode, ScaleDirection.UP);
        }

        List<NodeInfo> layer = getEngine().getCompleteDAG().getEntireLayer(newNode);
        getLogger().log(Level.INFO, "~~~~ Identify SiblingNodes, total nodes in current layer" +
                        layer.size() +
                "Nodes: " + layer
                );

        // drain sibling worker input&output
        for(NodeInfo siblingNode : layer)
        {
            if(siblingNode.equals(newNode))
            {
                continue;
            }
            getEngine().drainNodePipeline(siblingNode);
        }
        // drain sink input&output
        for(NodeInfo nodeInfo : newNode.getDownstream())
        {
            getEngine().drainNodePipeline(nodeInfo);
        }

        // migrate state
        // migrating state from existing nodes to new node
        for(NodeInfo siblingNode : layer)
        {
            if(siblingNode.equals(newNode))
            {
                continue;
            }
            getEngine().stateMigrate(siblingNode, newNode, ScaleDirection.UP);
        }

        // TODO: uncomment here for testing
        try {
            getLogger().log(Level.INFO, "$$$$$$$$$$$$$$$$ Start sleeping for 5 seconds $$$$$$$$$$$$$$$$$");
            Thread.sleep(5000);
            getLogger().log(Level.INFO, "$$$$$$$$$$$$$$$$ Wake up $$$$$$$$$$$$$$$$$");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // resume pipeline
        for(NodeInfo nodeInfo : newNode.getUpstream())
        {
            getEngine().resumePipeline(nodeInfo);
            getLogger().log(Level.INFO, "~~~~ Pipeline Resume Command Sent: " +
                    nodeInfo.getWorkerInfo().getOperatorType() +
                    "(" +
                    nodeInfo.getWorkerInfo().getAddress() +
                    ")~~~~");
        }
//        for(NodeInfo nodeInfo : newNode.getDownstream())
//        {
//            getEngine().resumePipeline(nodeInfo);
//        }
    }

    public void downScale(int layerIndex)
    {
        // stop source pipeline
        NodeInfo latestAddedNode = getEngine().getLatestAddedNode(layerIndex);
        for(NodeInfo nodeInfo : latestAddedNode.getUpstream())
        {
            getEngine().stallPipeline(nodeInfo);
        }
        // remove last added node
        NodeInfo removedNode = getEngine().getCompleteDAG().removeNode(layerIndex);

        // change source router
        for(NodeInfo nodeInfo : removedNode.getUpstream())
        {
            getEngine().updateNodeRouter(nodeInfo, removedNode, ScaleDirection.DOWN);
        }

        // migrate state
        // migrating state from removed node to existing nodes
        for(NodeInfo siblingNode : getEngine().getCompleteDAG().getEntireLayer(removedNode)) {
            getEngine().stateMigrate(removedNode, siblingNode, ScaleDirection.DOWN);
        }

        // resume pipeline
        for(NodeInfo nodeInfo : removedNode.getUpstream())
        {
            getEngine().resumePipeline(nodeInfo);
        }
    }
}
