package workers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import backends.DAG.WorkerInfo;
import operators.Operator;
import operators.OperatorFactory;
import operators.OperatorType;
import utils.MigrationMethod;
import utils.NetworkAddress;
import utils.StorageType;
import workers.buffer.LocalBuffer;
import workers.inputstream.InputStreamServer;
import workers.outstream.OutputStreamClientManager;

import java.util.logging.Logger;

/**
 * SimpleWorker
 *
 */
public class SimpleWorker extends Worker {
    private final static Logger logger = Logger.getLogger(SimpleWorker.class.getName());

    /**
     * Constructor
     *
     * @param address
     * @param operator
     * @param destinationSet
     */
    public SimpleWorker(UUID id, NetworkAddress address, Operator operator, Set<WorkerInfo> destinationSet, MigrationMethod method, StorageType storageType) {
        super(id, address, operator, destinationSet, method, storageType);
    }

    /**
     * Simple worker process entrance
     */
    public static void main(String[] args) {
        // START temp
        // Operator ope = new Operator();
        // ope.setType(OperatorType.SOURCE);
        // Set<Destination> destinationSet = new HashSet<>(){};
        // destinationSet.add(new Destination(SimpleWorker.DOMAIN, SimpleWorker.PORT));
        // SimpleWorker worker = new SimpleWorker(SimpleWorker.DOMAIN,
        // SimpleWorker.PORT, ope, destinationSet);
        // END temp

        // parse args
        logger.log(Level.INFO, Arrays.toString(args));
        UUID id = UUID.fromString(args[0]);
        String domain = args[1];
        int port = Integer.valueOf(args[2]);
        OperatorType type = OperatorType.toOperatorType(args[3]);
        Set<WorkerInfo> destSet = new HashSet<>();
        for (int destIdx = 4; destIdx < args.length; destIdx += 4) {
            destSet.add(new WorkerInfo(
                    new NetworkAddress(args[destIdx],
                            Integer.valueOf(args[destIdx + 1])),
                    OperatorType.toOperatorType(args[destIdx + 2]),
                    UUID.fromString(args[destIdx + 3])));
        }

        Operator ope = OperatorFactory.createOperator(type);
        SimpleWorker worker = new SimpleWorker(
                id,
                new NetworkAddress(domain, port),
                ope,
                destSet,
                MigrationMethod.PIPELINESTALLING,
                StorageType.HEAP);

        // start Input, Output stream
        worker.startInOutStream();

        logger.log(Level.INFO,
                "Worker started with operator " + worker.getOperator().getType() + ", on " + domain + ":" + port);

        // connect input output with operator
        worker.processData();

        worker.waitForInOutThreadFinish();
        logger.log(Level.INFO, "Worker {0} shutdown.", worker.getPort());
    }

    /**
     * Start input and output stream thread
     */
    public void startInOutStream() {
        if (isOutputStreamEnabled()) {
            OutputStreamClientManager outputStream = getOutputStream();
            outputStream.start();
        }

        if (isInputStreamEnabled()) {
            InputStreamServer inputStreams = getInputStreams();
            inputStreams.start();
        }
    }

    /**
     * input client place received data into input buffer
     * processData function get data from input buffer and process it using
     * operator.process() function
     * and place it into output buffer
     * output client get data from output buffer and send it to output stream
     */
    public void processData() {
        LocalBuffer<DataTuple> inputBuffer = getInputBuffer();
        LocalBuffer<DataTuple> outputBuffer = getOutputBuffer();
        DataTuple dataTuple;
        int tempCnt = 0;
        String[] tempData = { "apple", "zoo" };
        logger.log(Level.INFO, getOperator().getType() + " Start data Processing, isRunningValue:" + isRunning());
        while (isRunning()) {
            while (isPaused()) {
                synchronized(this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
//            setComputingData(true);
            if (isInputStreamEnabled() && !getOperator().getType().equals(OperatorType.SOURCE)) {
//                logger.log(Level.INFO, getOperator().getType() + " pulling data tuple from inputBuffer ");
                dataTuple = inputBuffer.getAndRemove();
                if (dataTuple == null) {
                    continue;
                }
                // logger.log(Level.INFO, getOperator().getType() + " query data from
                // inputBuffer");
//                if(getOperator().getType().equals(OperatorType.SINK))
//                    logger.log(Level.INFO, getOperator().getType() + "("+getAddress() + ") input buffer size: " +
//                            getInputBuffer().getSize());
//                logger.log(Level.INFO, getOperator().getType() + " Data loaded " + dataTuple.getData() + " : "
//                        + dataTuple.getCount());
            } else {
//                logger.log(Level.INFO, getOperator().getType() + " Generating data ");
                dataTuple = DataTuple.newBuilder().setReconfigRequest("none").setData(tempData[tempCnt %= 2])
                        .setCount(tempCnt++).build();
//                 logger.log(Level.INFO, getOperator().getType() + " Generate data " +dataTuple.getData() + " : "
//                         + dataTuple.getCount());
            }


//            if (getOperator().getType().equals(OperatorType.SOURCE)) {
//                try {
//                    Thread.sleep(0, 1);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }

            dataTuple = getOperator().process(getStorage(), dataTuple);

            if(dataTuple == null)
            {
                continue;
            }
            if (getOperator().getType().equals(OperatorType.SINK)) {
                logger.log(Level.INFO, getOperator().getType() + " Data processed " + dataTuple.getData() + " : "
                        + dataTuple.getCount());
            }
            if (isOutputStreamEnabled() && !getOperator().getType().equals(OperatorType.SINK)) {
                // logger.log(Level.INFO, getOperator().getType() + " inject data to
                // outputBuffer");
                outputBuffer.add(dataTuple);
//                if(getOperator().getType().equals(OperatorType.SOURCE))
//                    logger.log(Level.INFO, getOperator().getType() + "("+getAddress() + ") output buffer size: " +
//                            getOutputBuffer().getSize());
            }
//            setComputingData(false);
        }
    }

    /**
     * Wait for input and output stream thread to finish
     */
    public void waitForInOutThreadFinish() {
        try {
            if (isInputStreamEnabled()) {
                getInputStreams().join();
            }
            if (isOutputStreamEnabled()) {
                getOutputStream().join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
