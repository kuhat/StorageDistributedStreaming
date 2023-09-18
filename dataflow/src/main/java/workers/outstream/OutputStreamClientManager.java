package workers.outstream;

import io.grpc.StatusRuntimeException;
import operators.OperatorType;
import utils.NetworkAddress;
import workers.DataTuple;
import workers.buffer.LocalBuffer;
import workers.router.Router;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager the clients mapping and routing
 */
public class OutputStreamClientManager extends Thread{
    private final int MAX_BATCH_SIZE = 1500; // 500 , 1500 for testing, 1500 would take 90 seconds
    private final int timeoutMilliSec = 250;
    private final Timer timer;
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private LocalBuffer<DataTuple> outputBuffer;
    private OutputStreamClientCollection clientCollection;
    private Router router;
    private OperatorType operatorType;
    private String hostAddres;
    private boolean isPaused = false;

    /**
     * Constructor
     * @param type
     * @param hostAddress
     * @param outputBuffer
     */
    public OutputStreamClientManager(OperatorType type, String hostAddress, LocalBuffer outputBuffer)
    {
        this.clientCollection = new OutputStreamClientCollection();
        this.router = new Router();
        this.outputBuffer = outputBuffer;
        this.operatorType = type;
        this.hostAddres = hostAddress;
        this.timer = new Timer();
    }

    /**
     * Constructor
     * @param type
     * @param hostAddress
     * @param outputBuffer
     * @param clients
     */
    public OutputStreamClientManager(OperatorType type, String hostAddress,LocalBuffer outputBuffer, List<OutputStreamClient> clients)
    {
        this(type, hostAddress,outputBuffer);
        for(OutputStreamClient client : clients)
        {
            registerClient(client);
        }
    }

    /**
     * add a new edge towards down flow node
     * @param newClient
     */
    public void registerClient(OutputStreamClient newClient)
    {
        logger.log(Level.INFO, "Registering new client: " + newClient.getClientID());
        this.clientCollection.add(newClient);
        router.addNode(newClient.getClientID());
        if(operatorType == OperatorType.SOURCE)
        {
            logger.log(Level.INFO, "Router after adding new client: " + router.getCurrentRoutingPlan());
        }
    }

    public void unregisterClient(NetworkAddress address)
    {
        logger.log(Level.INFO, "Unregistering client: " + address);
        UUID removedID = this.clientCollection.remove(address);
        router.removeNode(removedID);
    }

    /**
     *  start thread
     */
    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println(">>>> " + OutputStreamClientManager.this.operatorType + " " + hostAddres +" Output Stream Shut Down");
            }
        });
        while (outputBuffer != null) {
            while (isPaused()) {
                logger.log(Level.INFO, operatorType + "(" +hostAddres + ") output stream is paused");
                synchronized(this) {
                    try {
                        wait();
                        logger.log(Level.INFO, operatorType + "(" +hostAddres + ") output stream is resumed");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            DataTuple dataTuple = outputBuffer.getAndRemove();
            if (dataTuple != null) {
                routeDataToClient(dataTuple);
            }
        }
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                patchData();
//            }
//        };
//        timer.schedule(task, timeoutMilliSec);
    }

    /**
     * route data to the corresponding client
     * @param dataTuple
     */
    public void routeDataToClient(DataTuple dataTuple)
    {
        UUID clientID = this.router.routeByKey(dataTuple.getData());
        if(clientID == null)
        {
            logger.log(Level.WARNING, "Router.routeByKey() returned null. Router cannot find node partition to match data: " + dataTuple.getData());
            return;
        }
        OutputStreamClient client = clientCollection.get(clientID);
//        logger.log(Level.INFO, "Router.routeByKey() returned <" + client.getTargetAddress() + "> Sending data: " + dataTuple.getData());
        client.sendDataDownFlow(dataTuple);
    }

//    public void patchData()
//    {
//        if(outputBuffer.getSize() == 0 || isPaused)
//        {
//            return;
//        }
//
//        try{
//            synchronized (outputBuffer)
//            {
//                Long batchSize = Math.min(MAX_BATCH_SIZE, outputBuffer.getSize());
////                if(outputBuffer.getSize() > MAX_BATCH_SIZE)
////                {
////                    logger.log(Level.WARNING, "!----!" + operatorType + "(" + hostAddres + ") outputBuffer size is less than max batch size: " + batchSize +"/" + MAX_BATCH_SIZE);
////                }
//                for(int i = 0; i < batchSize; i++)
//                {
//                    routeDataToClient(outputBuffer.getAndRemove());
//                }
//            }
//        } catch (StatusRuntimeException e){
//            // Cancel RPC
//            logger.log(Level.WARNING,"RPC failed: {0}" + e.getStatus());
//        }
//    }

    public OutputStreamClientCollection getClientCollection() {
        return clientCollection;
    }

    public void setClientCollection(OutputStreamClientCollection clientCollection) {
        this.clientCollection = clientCollection;
    }

    public Router getRouter() {
        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
        if(isPaused == false)
        {
            synchronized (this)
            {
                notifyAll();
            }
        }
    }
}
