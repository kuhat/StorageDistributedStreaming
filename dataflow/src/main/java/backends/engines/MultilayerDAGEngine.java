package backends.engines;

import backends.DAG.CompleteDAG;
import backends.DAG.NodeInfo;
import backends.DAG.WorkerInfo;
import backends.IDAGReconfigCallback;
import backends.communication.input.InputCommandServer;
import backends.communication.output.OutputCommandClient;
import backends.migration.MigrationManagerFactory;
import backends.migration.strategy.MigrationManager;
import operators.OperatorType;
import resourceMgr.ResourceMgr;
import utils.*;
import workers.ProcessManager;
import workers.SimpleWorker;
import workers.router.Router;
import backends.DAG.WorkerInfo;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * implementation of DAG(Directed Acyclic Graph) engine
 * 1. create a DAG by defining layers and operators
 * 2. start the DAG
 */
public class MultilayerDAGEngine extends DAGEngine
{
    private final static Logger logger = Logger.getLogger(MultilayerDAGEngine.class.getName());
    private NetworkAddress engineAddress;
    private final String LOCAL_DOMAIN = "localhost";

    // layers of OperationLayer{cnt, OperatorType}
    private List<OperationLayer> operationLayers;
    // layers of nodes(processes) in the DAG
    private CompleteDAG completeDAG;
    private ProcessManager processManager;
    private InputCommandServer inputCommandServer;
    private MigrationManager migrationManager;
    private UUID engineID;
    private Router sourceRouter; // trying to mimic the routing of source
    private Map<OperatorType, Queue<WorkerInfo>> additionalWorkerMap;
    /**
     * constructor
     * @param opLayer
     */
    public MultilayerDAGEngine(List<OperationLayer> opLayer, boolean isLocalHost, MigrationMethod migrationMethod)
    {
        this.engineID = UUID.randomUUID();
        this.operationLayers = opLayer;
        this.completeDAG = new CompleteDAG(opLayer);
        String ipAddress = LOCAL_DOMAIN;
        this.sourceRouter = new Router();
        this.additionalWorkerMap = new HashMap<>();
        if(!isLocalHost)
        {
            ipAddress = PortManager.getIPAddress();
        }
        this.engineAddress = new NetworkAddress(ipAddress, PortManager.getFreePort());
        IDAGReconfigCallback reconfigCallback = (int layerIndex, ScaleDirection scaleDirection) -> {
            this.performReconfig(layerIndex, scaleDirection);
        };
        this.inputCommandServer = new InputCommandServer(engineAddress, reconfigCallback);
        this.migrationManager = MigrationManagerFactory.create(migrationMethod, this);
        startProcessManager();
    }

    private void startProcessManager()
    {
        this.processManager = new ProcessManager();
        this.processManager.start();
    }

    /**
     * start the DAG
     */
    public void start()
    {
        //  register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                MultilayerDAGEngine.this.processManager.terminateAll();
                System.err.println("*** Application Shutdown");
            }
        });
        // start reconfigure receiving service
        inputCommandServer.start();

        // start default worker processes
        initOperationalLayers();

        // start additional worker processes
        initAdditionalWorker(OperatorType.COUNT, 1);

        // start reconfigure manager service
        initReconfig();

        try {
            this.processManager.join();
            this.inputCommandServer.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * backwards start processes layer by layer
     * 1. start the last layer
     * 2. start the second last layer, and connect to the last layer
     * 3. ...
     *
     */
    private void initOperationalLayers()
    {
        logger.log(Level.INFO, "====================START OPERATIONAL LAYERS PROCESS====================");
        // start base nodes, sink, counter, source
        for(int currentlayerNum = this.operationLayers.size() - 1; currentlayerNum >= 0; currentlayerNum--)
        {
            OperationLayer layer = operationLayers.get(currentlayerNum);
            int operatorCnt = layer.getOperationCnt();
            OperatorType opType = layer.getType();
            for(int currrentOpNum = 0; currrentOpNum < operatorCnt; currrentOpNum++)
            {
                // start worker jvm
                WorkerInfo workerInfo = startWorker(opType);
                // add a fake uuid to fake source router for routing compute
                if(opType == OperatorType.COUNT) {
                    this.sourceRouter.addNode(workerInfo.getId());
                }
            }
        }
        logger.log(Level.INFO, "====================FINISHED OPERATIONAL LAYERS PROCESS INITIATION====================");
    }

    // TODO: extend to support more operators
    private void initAdditionalWorker(OperatorType type, int num)
    {
        if(!type.equals(OperatorType.COUNT))
        {
            logger.log(Level.SEVERE, "!!!!!!!!!!!only support count operator!!!!!!!!!!!");
        }
        // start worker processes
        for(int i = 0; i < num; i++)
        {
            NetworkAddress networkAddress = new NetworkAddress(LOCAL_DOMAIN, PortManager.getFreePort());
            UUID workerID = UUID.randomUUID();
            OutputCommandClient outputCommandClient = new OutputCommandClient(this.engineID, workerID, getEngineAddress(), networkAddress);

            /** START assemble process arguments **/
            List<String> args = new LinkedList<>();
            args.add(workerID.toString());
            args.add(networkAddress.getDomain()); // domain
            args.add(String.valueOf(networkAddress.getPort()));// port
            args.add(type.toString()); // Operator

            // out flow connect to sink
            for(NodeInfo downflowNode : getCompleteDAG().getNodeLayer(OperatorType.SINK))
            {
                args.add(downflowNode.getWorkerInfo().getDomain());
                args.add(String.valueOf(downflowNode.getWorkerInfo().getPort()));
                args.add(String.valueOf(downflowNode.getWorkerInfo().getOperatorType()));
                args.add(String.valueOf(downflowNode.getWorkerInfo().getId()));
            }
            /** END assemble process arguments **/

            // start process in new jvm
            try {
                processManager.exec(SimpleWorker.class, args);
                Thread.sleep(200);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            WorkerInfo workerInfo = new WorkerInfo(networkAddress, type, workerID, outputCommandClient);

            if(!this.additionalWorkerMap.containsKey(type))
            {
                this.additionalWorkerMap.put(type, new LinkedList<>());
            }
            this.additionalWorkerMap.get(type).add(workerInfo);
        }
    }


    private void initReconfig(){
        List<String> arg = new ArrayList<>();
        arg.add(getEngineAddress().getDomain());
        arg.add(String.valueOf(getEngineAddress().getPort()));
        arg.add("reconfigurationClient");
        try {
            processManager.exec(ResourceMgr.class, arg);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private WorkerInfo startWorker(OperatorType opType)
    {
        logger.log(Level.INFO, "-----------------START WORKER PROCESS-----------------");

        // fill in an empty node slot in DAG
        NetworkAddress networkAddress = new NetworkAddress(LOCAL_DOMAIN, PortManager.getFreePort());
        UUID workerID = UUID.randomUUID();
        // TODO: split the filling worker info and starting worker process
        OutputCommandClient outputCommandClient = new OutputCommandClient(this.engineID, workerID, this.engineAddress, networkAddress);
        WorkerInfo workerInfo = new WorkerInfo(networkAddress, opType, workerID, outputCommandClient);
        NodeInfo nodeInfo = completeDAG.addRunningWorkerToExecutionGraph(workerInfo);

        // assemble process arguments
        List<String> args = new LinkedList<>();
        args.add(workerID.toString()); // workerID
        args.add(networkAddress.getDomain()); // domain
        args.add(String.valueOf(networkAddress.getPort()));// port
        args.add(opType.toString()); // Operator
        for(NodeInfo downflowNode : nodeInfo.getDownstream())
        {
            args.add(downflowNode.getWorkerInfo().getDomain());
            args.add(String.valueOf(downflowNode.getWorkerInfo().getPort()));
            args.add(String.valueOf(downflowNode.getWorkerInfo().getOperatorType()));
            args.add(String.valueOf(downflowNode.getWorkerInfo().getId()));
        }

        // start process in new jvm
        try {
            processManager.exec(SimpleWorker.class, args);
            Thread.sleep(1000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        logger.log(Level.INFO, "-----------------FINISHED WORKER PROCESS INITIATION-----------------");
        return workerInfo;
    }

    public void updateWorkerInfoInDAG(WorkerInfo workerInfo)
    {

    }

    public void drainNodePipeline(NodeInfo nodeInfo)
    {
        nodeInfo.getWorkerInfo().getOutputCommandClient().sendDrainPipelineCommand();
        logger.log(Level.INFO, "~~~~ Pipeline Draining Command Sent: " +
                nodeInfo.getWorkerInfo().getOperatorType() +
                "(" +
                nodeInfo.getWorkerInfo().getAddress() +
                ")~~~~");
    }

    public NodeInfo connectAdditionalWorkerToGraph(OperatorType operatorType)
    {
        if(!operatorType.equals(OperatorType.COUNT))
        {
            logger.log(Level.SEVERE, "only support count operator");
        }

        // take awaiting worker from the map
        WorkerInfo availableWorker = getAdditionalWorkerMap().get(operatorType).poll();

        // add new node to the graph
        NodeInfo newNode = getCompleteDAG().addNode(operatorType);
        // add it to the graph
        NodeInfo nodeInfo = completeDAG.addRunningWorkerToExecutionGraph(availableWorker);

        // add fake UUID to the fake source router here just make it happy
        this.sourceRouter.addNode(availableWorker.getId());

        logger.log(Level.INFO, "~~~~ New node added: " + newNode.getWorkerInfo().getOperatorType() + "(" + newNode.getWorkerInfo().getAddress()+")~~~~");
        return newNode;
    }

    public void stallPipeline(NodeInfo nodeInfo)
    {
        logger.log(Level.INFO, "~~~~ Pipeline Stall Command Sent: " +
                nodeInfo.getWorkerInfo().getOperatorType() +
                "(" +
                nodeInfo.getWorkerInfo().getAddress() +
                ")~~~~");
        nodeInfo.getWorkerInfo().getOutputCommandClient().sendStallPipelineCommand();
    }

    public void resumePipeline(NodeInfo nodeInfo)
    {
        nodeInfo.getWorkerInfo().getOutputCommandClient().sendResumePipelineCommand();
    }

    public void updateNodeRouter(NodeInfo sourceNode, NodeInfo changedNode, ScaleDirection scaleDirection)
    {
        sourceNode.getWorkerInfo().getOutputCommandClient().sendReconfigRouterCommand(changedNode, scaleDirection);
        logger.log(Level.INFO, "~~~~ Source Router change Command Sent: " +
                sourceNode.getWorkerInfo().getOperatorType() +
                "(" +
                sourceNode.getWorkerInfo().getAddress() +
                ") " +
                scaleDirection.toString() +
                "~~~~");
    }

    public void stateMigrate(NodeInfo siblingNode, NodeInfo newNode, ScaleDirection direction)
    {
        Set<Character> routingDiff = computeRoutingDiff(siblingNode, direction);
        siblingNode.getWorkerInfo().getOutputCommandClient().
                sendStateMigrationCommand(newNode.getWorkerInfo(), routingDiff);
        logger.log(Level.INFO, "~~~~ Migrate Data Command Sent: " +
                siblingNode.getWorkerInfo().getOperatorType() +
                "(" +
                siblingNode.getWorkerInfo().getAddress() +
                ") " +
                "to " +
                newNode.getWorkerInfo().getOperatorType() +
                "(" +
                newNode.getWorkerInfo().getAddress() +
                ") with keys" +
                routingDiff +
                "~~~~");
    }

    private Set<Character> computeRoutingDiff(NodeInfo nodeInfo, ScaleDirection direction)
    {
        int shift = 0;
        switch(direction)
        {
            case UP:
                // compute routing plan from current# vs current# - 1
                shift = -1;
                break;
            case DOWN:
                shift = 1;
                // compute routing plan from current# vs current# + 1
                break;
            default:
                throw new RuntimeException("invalid scale direction");
        }
//        int nodeIndex = getCompleteDAG().getEntireLayer(nodeInfo).indexOf(nodeInfo);
        Set<Character> keyDiff = this.sourceRouter.getKeyDiff(nodeInfo.getWorkerInfo().getId(), shift);

//        Map<Integer, Set<Character>> indexToKeyDiff;
////        int nodeCnt = getCompleteDAG().getEntireLayer(nodeInfo).size();
//        Router router = new Router();
//        switch (direction) {
//            case UP:
//                // compute routing plan from current# vs current# - 1
//                indexToKeyDiff = router.getPlanDiff(2, 3);
//                break;
//            case DOWN:
//                // compute routing plan from current# vs current# + 1
//                indexToKeyDiff = router.getPlanDiff(3, 2);
//                break;
//            default:
//                throw new RuntimeException("invalid scale direction");
//        }
//        Map<NetworkAddress,  Set<Character>> res = new HashMap<>();
//        for(Map.Entry<Integer, Set<Character>> entry : indexToKeyDiff.entrySet())
//        {
//            NetworkAddress address = getCompleteDAG().getEntireLayer(nodeInfo).get(entry.getKey()).getWorkerInfo().getAddress();
//            res.put(address, entry.getValue());
//        }

        logger.log(Level.INFO, nodeInfo + " will send out following keys:" + keyDiff );

        return keyDiff;
    }

    public NodeInfo getLatestAddedNode(int layerIndex)
    {
        return this.completeDAG.getLatestAddedNode(layerIndex);
    }

    /**
     * call back impl
     * perform reconfiguration
     */
    private void performReconfig(int layerIndex, ScaleDirection scaleDirection)
    {
        if(scaleDirection == ScaleDirection.UP)
        {
            this.migrationManager.upScale(layerIndex);
        }
        else if(scaleDirection == ScaleDirection.DOWN)
        {
            this.migrationManager.downScale(layerIndex);
        }
        else
        {
            throw new RuntimeException("invalid scale direction");
        }
    }

    // getters and setters
    public NetworkAddress getEngineAddress() {
        return engineAddress;
    }

    public void setEngineAddress(NetworkAddress engineAddress) {
        this.engineAddress = engineAddress;
    }

    public CompleteDAG getCompleteDAG() {
        return completeDAG;
    }

    public void setCompleteDAG(CompleteDAG completeDAG) {
        this.completeDAG = completeDAG;
    }

    public MigrationManager getMigrationManager() {
        return migrationManager;
    }

    public void setMigrationManager(MigrationManager migrationManager) {
        this.migrationManager = migrationManager;
    }

    public UUID getEngineID() {
        return engineID;
    }

    public void setEngineID(UUID engineID) {
        this.engineID = engineID;
    }

    public Map<OperatorType, Queue<WorkerInfo>> getAdditionalWorkerMap() {
        return additionalWorkerMap;
    }

    public void setAdditionalWorkerMap(Map<OperatorType, Queue<WorkerInfo>> additionalWorkerMap) {
        this.additionalWorkerMap = additionalWorkerMap;
    }

    public Router getSourceRouter() {
        return sourceRouter;
    }

    public void setSourceRouter(Router sourceRouter) {
        this.sourceRouter = sourceRouter;
    }
}
