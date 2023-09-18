//package operators;
//
//import org.apache.flink.api.common.functions.FilterFunction;
//import utils.TimedMessageEvent;
//
//import java.util.concurrent.ConcurrentLinkedQueue;
//
//public class FilterOperator extends SingleInputOperator<TimedMessageEvent, TimedMessageEvent> {
//    // filter using lambda expression
//    private final FilterFunction<TimedMessageEvent> filterFunction;
//
//
//    public FilterOperator(ConcurrentLinkedQueue<TimedMessageEvent> input, ConcurrentLinkedQueue<TimedMessageEvent> output,
//                          FilterFunction<TimedMessageEvent> filterFunction) {
//        super(input, output);
//        this.filterFunction = filterFunction;
//    }
//
//
//    public void processElement(TimedMessageEvent input) throws Exception {
//        if (filterFunction.filter(input)) {
//            emit(input);
//        }
//    }
//
//    @Override
//    public void run() {
//        while (true) {
//            if (!input.isEmpty()) {
//                TimedMessageEvent element = input.poll();
//                if (element != null) {
//                    try {
//                        processElement(element);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        }
//    }
//
//    // Unit Test
//    public static void main(String[] args) {
//        // using wikipedia source operator
//
//    }
//}
