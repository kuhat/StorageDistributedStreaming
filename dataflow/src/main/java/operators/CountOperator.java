package operators;

import workers.DataTuple;
import workers.storage.IKVStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CountOperator extends Operator {
    private final HashMap<String, Integer> map;

    public CountOperator() {
        setType(OperatorType.COUNT);
        map = new HashMap<>();
    }

    @Override
    public DataTuple process(IKVStorage storage, DataTuple dataTuple) {
        DataTuple.Builder builder = DataTuple.newBuilder(dataTuple);
        String word = dataTuple.getData();
        int count = 0;
        if (!word.equals("END_OF_FILE")) {
            if (storage.containsKey(word)) {
//                count = Integer.parseInt((String) storage.get(word));
                count = (int) storage.get(word);
            }
            count++;
//            map.put(word, count);
            storage.put(word, count);
        }
        builder.setData(word);
        builder.setCount(count);
        fibonacci(23);
        return builder.build();
    }

    private int fibonacci(int n) {
        if (n <= 1 && n >= 0) {
            return n;
        } else {
            return fibonacci(n - 1) + fibonacci(n - 2);
        }
    }

    public static void main(String[] args) throws IOException {
        DataTuple dataTuple = DataTuple.newBuilder().setReconfigRequest("none").setData("INIT").setCount(0).build();
        FileSourceOperator fileSourceOperator = new FileSourceOperator(
                "dataflow/TheCompleteWorksOfWilliamShakespearebyWilliamShakespeare.txt");
        CountOperator countOperator = new CountOperator();
        SinkOperator sinkOperator = new SinkOperator("output.json");
        sinkOperator.setAnalysis(true);
        ArrayList<Operator> operators = new ArrayList<>();
        operators.add(fileSourceOperator);
        operators.add(countOperator);
        operators.add(sinkOperator);
        while (!dataTuple.getData().equals("END_OF_FILE")) {
            for (Operator operator : operators) {
                dataTuple = operator.process(null, dataTuple);
            }
            // System.out.println(dataTuple.getData() + " " + dataTuple.getCount());
        }
    }
}
