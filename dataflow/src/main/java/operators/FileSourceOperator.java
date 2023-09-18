package operators;

import utils.WordSplitter;
import workers.DataTuple;
import workers.storage.IKVStorage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileSourceOperator extends SourceOperator {
    private final String filePath;
    private ArrayList<String> wordBuffer;
    private BufferedReader reader;
    private int wordBufferIndex;
    private boolean loop = false;
    private boolean endOfFile = false;

    public FileSourceOperator(String filePath) throws IOException {
        this.filePath = filePath;
        this.wordBuffer = new ArrayList<>();
        this.wordBufferIndex = 0;
        loadFile();
        warmUp();
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean getLoop() {
        return loop;
    }

    private void loadFile() throws IOException {
        File file = new File(filePath);
        reader = new BufferedReader(new FileReader(file));
        // String line;
        // while ((line = reader.readLine()) != null) {
        // wordBuffer.addAll(List.of(WordSplitter.split(line)));
        // }
    }

    private ArrayList<String> readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        return new ArrayList<>(List.of(WordSplitter.split(line)));
    }

    private void warmUp(){  // read 1000 lines to word-buffer
        wordBuffer = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            try {
                wordBuffer.addAll(readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public DataTuple process(IKVStorage storage, DataTuple dataTuple) {
        DataTuple.Builder builder = DataTuple.newBuilder(dataTuple);
        try {
            if (wordBuffer == null) { // End of file
                if (loop) {
                    loadFile();
                    wordBuffer = readLine();
                } else {
                    reader.close();
                    return cleanUp();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (wordBufferIndex >= wordBuffer.size()) {
            // Read a new line
            try {
                wordBuffer = readLine();
                while (wordBuffer != null && wordBuffer.size() == 0) {
                    wordBuffer = readLine();
                }
                if (wordBuffer == null) { // End of file
                    if (loop) {
                        loadFile();
                        wordBuffer = readLine();
                    } else {
                        reader.close();
                        return cleanUp();
                    }
                }
                wordBufferIndex = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String word = wordBuffer.get(wordBufferIndex);
        builder.setData(word);
        builder.setCount(0);
        // builder.setTimestamp(System.currentTimeMillis());
        builder.setTimestamp(System.nanoTime()); // This is used for critical analysis
        wordBufferIndex++;
        return builder.build();
    }

    private DataTuple cleanUp() {
        if (endOfFile) {
            return null;
        }
        endOfFile = true;
        DataTuple.Builder builder = DataTuple.newBuilder();
        builder.setData("END_OF_FILE");
        // builder.setTimestamp(System.currentTimeMillis());
        builder.setTimestamp(System.nanoTime()); // This is used for critical analysis
        return builder.build();
    }

    @Override
    public List<DataTuple> process(IKVStorage storage, List<DataTuple> dataTuples) {
        List<DataTuple> result = new ArrayList<>();
        for (DataTuple dataTuple : dataTuples) {
            result.add(process(storage, dataTuple));
            if (dataTuple.getData().equals("END_OF_FILE")) {
                break;
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        DataTuple dataTuple = DataTuple.newBuilder().setReconfigRequest("none").setData("INIT").setCount(1).build();
        FileSourceOperator fileSourceOperator = new FileSourceOperator(
                "dataflow/TheCompleteWorksOfWilliamShakespearebyWilliamShakespeare.txt");
        while (!dataTuple.getData().equals("END_OF_FILE")) {
            dataTuple = fileSourceOperator.process(null, dataTuple);
            System.out.println(dataTuple.getData() + " " + dataTuple.getCount());
        }
    }
}
