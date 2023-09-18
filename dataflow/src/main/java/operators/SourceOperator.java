package operators;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class SourceOperator extends Operator{
    public SourceOperator(){
        setType(OperatorType.SOURCE);
    }

}
