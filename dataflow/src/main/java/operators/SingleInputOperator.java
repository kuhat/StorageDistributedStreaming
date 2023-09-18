package operators;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class SingleInputOperator<IN, OUT> extends Thread {
    /**
     * Apply transformation
     * @param element input event
     */

//    protected ConcurrentLinkedQueue<IN> input;
//    protected ConcurrentLinkedQueue<OUT> output;

    public SingleInputOperator() {
//        this.input = input;
//        this.output = output;
    }

    public abstract void processElement(IN input) throws Exception;

    protected void emit(OUT element) {
    }

}
