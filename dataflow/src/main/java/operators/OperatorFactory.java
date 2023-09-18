package operators;

import java.io.IOException;

public class OperatorFactory {
    public static final String FILESOURCE = "dataflow/TheCompleteWorksOfWilliamShakespearebyWilliamShakespeare.txt";
    public static final String TESTSOURCE_APPLEZOO = "dataflow/applezoo.txt";
    public static final String TESTSOURCE_ALPHABET = "dataflow/alphabet.txt";
    public static final String OUTPUTFILE = "dataflow/output.json";
    public static Operator createOperator(OperatorType type)
    {
        Operator operator = null;
        switch(type)
        {
            case SOURCE:
                try {
                    operator = new FileSourceOperator(FILESOURCE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case COUNT:
                operator = new CountOperator();
                break;
            case SINK:
                operator = new SinkOperator(OUTPUTFILE);
                ((SinkOperator)operator).setAnalysis(true);
                break;
            default:
                break;
        }
        return operator;
    }
}
