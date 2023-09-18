import kotlin.Pair;
import operators.CountOperator;
import operators.FileSourceOperator;
import operators.Operator;
import operators.SinkOperator;
import org.junit.Test;
import workers.DataTuple;
import workers.storage.RocksKVStorage;

import java.io.IOException;
import java.util.ArrayList;

public class BaselineModelTest {
    @Test
    public void test() throws IOException {
        DataTuple dataTuple = DataTuple.newBuilder().setReconfigRequest("none").setData("INIT").setCount(0).build();
        FileSourceOperator fileSourceOperator = new FileSourceOperator("TheCompleteWorksOfWilliamShakespearebyWilliamShakespeare.txt");
        CountOperator countOperator = new CountOperator();
        SinkOperator sinkOperator = new SinkOperator("output.txt");
        sinkOperator.setAnalysis(true);
        ArrayList<Operator> operators = new ArrayList<>();
        operators.add(fileSourceOperator);
        operators.add(countOperator);
        operators.add(sinkOperator);
        RocksKVStorage<String, Integer> storage = new RocksKVStorage<>("testdb");
        while (!dataTuple.getData().equals("END_OF_FILE")) {
            for (Operator operator : operators) {
                dataTuple = operator.process(storage, dataTuple);
            }
//            System.out.println(dataTuple.getData() + " " + dataTuple.getCount());
        }
        for (Pair<String, Integer> pair : storage.getAll()) {
            System.out.println(pair.getFirst() + " " + pair.getSecond());
        }
    }
}
