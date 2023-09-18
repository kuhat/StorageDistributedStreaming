package workers;

import backends.DAG.WorkerInfo;
import operators.Operator;
import operators.OperatorType;
import utils.MigrationMethod;
import utils.NetworkAddress;
import utils.ScaleDirection;
import utils.StorageType;
import workers.buffer.LocalBuffer;
import workers.inputstream.InputStreamServer;
import workers.outstream.OutputStreamClient;
import workers.outstream.OutputStreamClientManager;
import workers.storage.IKVStorage;
import workers.storage.StorageManager;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * abstract root/base class of Worker
 */
public abstract class Worker implements IRunnableWorker {
    private Operator operator;
    private LocalBuffer<DataTuple> inputBuffer, outputBuffer;
    private IKVStorage<String, Integer> storage;
    private StorageManager storageManager;
    private InputStreamServer inputStreams;
    private OutputStreamClientManager outputStream;
    private UUID workerID;
    private boolean isInputStreamEnabled;
    private boolean isOutputStreamEnabled;
    private boolean isRunning;
    private boolean isPaused;
    private boolean isComputingData;
    private NetworkAddress address;

    /**
     * constructor
     * @param address
     * @param operator
     * @param downflowWorkerInfoSet
     */
    public Worker(UUID id,
                  NetworkAddress address,
                  Operator operator,
                  Set<WorkerInfo> downflowWorkerInfoSet,
                  MigrationMethod method,
                  StorageType storageType) {
        setAddress(address);
        setOperator(operator);
        setStorageManager(new StorageManager<String, Integer>(getOperator().getType(), storageType, getDomain(), getPort()));
        setStorage(getStorageManager().getStorage());
        setInputStreamEnabled(OperatorType.isInputStreamEnabled(operator.getType()));
        setOutputStreamEnabled(OperatorType.isOutputStreamEnabled(operator.getType()));
        setWorkerID(id);
        if(isInputStreamEnabled())
        {
            setInputBuffer(new LocalBuffer<>());
            setInputStreams(new InputStreamServer(
                    getOperator().getType(),
                    address,
                    getInputBuffer(),
                    getOperationSwitchCallBack(),
                    getRouterUpdateCallBack(),
                    getStateMigrationCallBack(),
                    getMergeStateCallBack(),
                    getDrainCallBack()));
        }
        if(isOutputStreamEnabled()){
            setOutputBuffer(new LocalBuffer<>());
            List<OutputStreamClient> clients = new LinkedList<>();
            for(WorkerInfo downflowWorkerInfo : downflowWorkerInfoSet)
            {
                clients.add(new OutputStreamClient(getUUId(),
                        downflowWorkerInfo.getId(),
                        getOperator().getType(),
                        address,
                        downflowWorkerInfo.getAddress()));
            }
            setOutputStream(new OutputStreamClientManager(getOperator().getType(), address.toString(), getOutputBuffer(), clients));
        }
        setRunning(true);
        setPaused(false);
        setComputingData(false);
    }

    public void pauseStream()
    {
        setPaused(true);
        getOutputStream().setPaused(true);
    }

    public void resumeStream()
    {
        setPaused(false);
        getOutputStream().setPaused(false);
        // wake up thread
        synchronized (this)
        {
            notifyAll();
        }
    }

    public void drainInputStream()
    {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, this.getOperator() + "(" + getAddress() + ") Draining input stream...");
//        Timer timer = new Timer();
//        final boolean[] isDrained = {false};
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                isDrained[0] = getInputBuffer().getSize() == 0 && getOutputBuffer().getSize() == 0;
////                Logger.getLogger("Worker").log(Level.INFO, "isDrained:" + isDrained[0]);
//                Logger.getLogger("Worker").log(Level.INFO, "Input Output size: " + getInputBuffer().getSize() + " & " +getOutputBuffer().getSize());
//                if(isDrained[0])
//                {
////                    Logger.getLogger("Worker").log(Level.INFO, "Drained");
//                    timer.cancel();
//                }
//            }
//        };
//        timer.schedule(task,0, 100);
        boolean isDrained = getInputBuffer().getSize() == 0 && getOutputBuffer().getSize() == 0;
        while(!isDrained)
        {
            try {
                Thread.sleep(200); // see if there is any data that under processing
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            isDrained = getInputBuffer().getSize() == 0 && getOutputBuffer().getSize() == 0;
//            Logger.getLogger("Worker").log(Level.INFO, getOperator() + "("+ getAddress() +") isDrained:" + isDrained);
//            Logger.getLogger("Worker").log(Level.INFO, getOperator() + "("+ getAddress() +") Input Output size: " + getInputBuffer().getSize() + " & " +getOutputBuffer().getSize());
        }
        Logger.getLogger("Worker").log(Level.INFO, "Drained");

        try {
            Thread.sleep(2000); // see if there is any data that under processing
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // callback function to pause or resume stream
    public StreamOperationSwitchCallBack getOperationSwitchCallBack()
    {
        return () -> {
            if (isPaused()) {
                resumeStream();
            } else {
                pauseStream();
            }
        };
    }

    public StreamRouterUpdateCallBack getRouterUpdateCallBack()
    {
        return (OperatorType operatorType, UUID id, NetworkAddress changedAddress, ScaleDirection scaleDirection)->{
            switch (scaleDirection)
            {
                case UP:
                    addDownFlowClient(operatorType, id, changedAddress);
                    break;
                case DOWN:
                    removeDownFlowClient(changedAddress);
                    break;
            }
        };
    }

    public StreamStateMigrationCallBack getStateMigrationCallBack()
    {
        return (WorkerInfo targetWorkerInfo, Set<String> keys)->
        {
            OutputStreamClient tempClient = new OutputStreamClient(getUUId(),targetWorkerInfo.getId(),  targetWorkerInfo.getOperatorType(), getAddress(), targetWorkerInfo.getAddress());
            tempClient.sendState(getStorageManager().getStorage(), keys);
        };
    }

    public StreamMergeStateCallBack getMergeStateCallBack()
    {
        return (DataBatch dataBatch) ->
        {
            integrateState(dataBatch);
        };
    }

    public StreamDrainCallBack getDrainCallBack()
    {
        return ()->
        {
            drainInputStream();
        };
    }


    public void integrateState(DataBatch dataBatch)
    {
        for(DataTuple dataTupe : dataBatch.getDataList())
        {
            getStorage().put(dataTupe.getData(), dataTupe.getCount());
        }
    }

    public void addDownFlowClient(OperatorType operatorType, UUID clientID, NetworkAddress networkAddress)
    {
        getOutputStream().registerClient(new OutputStreamClient(getUUId(), clientID, operatorType, getAddress(), networkAddress));
    }

    public void removeDownFlowClient(NetworkAddress networkAddress)
    {
        getOutputStream().unregisterClient(networkAddress);
    }

    /**
     * getters and setters
     */

    public NetworkAddress getAddress() {
        return address;
    }

    public void setAddress(NetworkAddress address) {
        this.address = address;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public LocalBuffer<DataTuple> getInputBuffer() {
        return inputBuffer;
    }

    public void setInputBuffer(LocalBuffer<DataTuple> inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public LocalBuffer<DataTuple> getOutputBuffer() {
        return outputBuffer;
    }

    public void setOutputBuffer(LocalBuffer<DataTuple> outputBuffer) {
        this.outputBuffer = outputBuffer;
    }

    public IKVStorage<String, Integer> getStorage() {
        return storage;
    }

    public void setStorage(IKVStorage<String, Integer> storage) {
        this.storage = storage;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public void setStorageManager(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public InputStreamServer getInputStreams() {
        return inputStreams;
    }

    public void setInputStreams(InputStreamServer inputStreams) {
        this.inputStreams = inputStreams;
    }

    public OutputStreamClientManager getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStreamClientManager outputStream) {
        this.outputStream = outputStream;
    }


    public boolean isInputStreamEnabled() {
        return isInputStreamEnabled;
    }

    public void setInputStreamEnabled(boolean inputStreamEnabled) {
        isInputStreamEnabled = inputStreamEnabled;
    }

    public boolean isOutputStreamEnabled() {
        return isOutputStreamEnabled;
    }

    public void setOutputStreamEnabled(boolean outputStreamEnabled) {
        isOutputStreamEnabled = outputStreamEnabled;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
    public String getDomain() {
        return address.getDomain();
    }

    public void setDomain(String domain) {
        this.address.setDomain(domain);
    }

    public int getPort() {
        return  this.address.getPort();
    }

    public void setPort(int port) {
        this.address.setPort(port);
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }
    public boolean isComputingData() {
        return isComputingData;
    }

    public void setComputingData(boolean computingData) {
        isComputingData = computingData;
    }
    public UUID getUUId() {
        return workerID;
    }

    public void setWorkerID(UUID workerID) {
        this.workerID = workerID;
    }
}
