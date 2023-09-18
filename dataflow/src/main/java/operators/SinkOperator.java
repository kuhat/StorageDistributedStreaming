package operators;

//import com.fasterxml.jackson.databind.ObjectMapper;
import workers.DataTuple;
import workers.storage.IKVStorage;

//import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SinkOperator extends Operator {

    private final String fileName;
    private final HashMap<String, Integer> map;
    private boolean analysis = true;
    private final Logger logger = Logger.getLogger(SinkOperator.class.getName());
    private final Lock lock = new ReentrantLock();
    private final Condition newInput = lock.newCondition();
    private boolean eof = false;


    public SinkOperator(String output){
        fileName = output;
        map = new HashMap<>();
        setType(OperatorType.SINK);
        Thread sinkThread = new Thread(this::runSink);
        sinkThread.start();
    }

    public void setAnalysis(boolean on) {
        analysis = on;
        if (analysis) {
            try {
                FileHandler fileHandler = new FileHandler("sinkOperator.log", false);
                fileHandler.setFormatter(new SimpleFormatter());
                // undisplayed the log in console
//                logger.setUseParentHandlers(false);
                logger.addHandler(fileHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public DataTuple process(IKVStorage storage, DataTuple dataTuple) {
        lock.lock();
        try{
            if (dataTuple.getData().equals("END_OF_FILE")) {
                eof = true;
            } else {
                eof = false;
                map.put(dataTuple.getData(),dataTuple.getCount());
            }
        } finally {
            lock.unlock();
        }
//        if (dataTuple.getData().equals("END_OF_FILE")) {
//            ObjectMapper mapper = new ObjectMapper();
//            try {
//                mapper.writeValue(new File(fileName), map);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return dataTuple;
//        }
//        map.put(dataTuple.getData(), dataTuple.getCount());
        if (analysis) {
            try {
                logger.info("SinkOperator[" + System.nanoTime() + "]: " + dataTuple.getData() + " " + dataTuple.getCount());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        storage.put(dataTuple.getData(),dataTuple.getCount());
        return dataTuple;
    }

    private void writeToFile() {
        try (FileWriter fileWriter = new FileWriter(Paths.get(fileName).toFile())) {
            String jsonString = mapToJson(map);
            fileWriter.write(jsonString);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private String mapToJson(HashMap<String, Integer> map) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        int count = 0;
        int size = map.size();
        for (String key : map.keySet()){
            jsonBuilder.append("\"").append(key);
            jsonBuilder.append("\"").append(map.get(key));
            if (++count < size) {
                jsonBuilder.append(", ");
            }
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    private String jsonStringEscape(String input) {
        return input.replace("\\","\\\\").replace("\"","\\\"");
    }

    private void runSink(){
        while (true) {
            lock.lock();
            try{
                while (!eof){
                    newInput.await();
                }
                boolean timeout = !newInput.await(10, TimeUnit.SECONDS);
                if (timeout) {
                    System.out.println("No new received data");
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        writeToFile();
    }
}
