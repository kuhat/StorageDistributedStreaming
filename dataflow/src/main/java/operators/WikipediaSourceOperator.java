package operators;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import utils.TimedMessageEvent;
import workers.DataTuple;
import workers.storage.IKVStorage;

import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

//public class WikipediaSourceOperator extends SourceOperator<TimedMessageEvent> {
//    private final String URL;
//    private final long timeout;
//
//    public WikipediaSourceOperator(String URL, long timeout, ConcurrentLinkedQueue<TimedMessageEvent> output) {
//        super(output);
//        this.URL = URL;
//        this.timeout = timeout;
//    }
//
//    @Override
//    public void run() {
//        startReceiving();
//    }
//
//    private void startReceiving() {
//        WikipediaEventHandler handler = new WikipediaEventHandler();
//        EventSource.Builder builder = new EventSource.Builder(handler, URI.create(URL));
//        try (EventSource eventSource = builder.build()) {
//            eventSource.start();
//            TimeUnit.SECONDS.sleep(timeout);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void processMessage(MessageEvent msg) {
//        TimedMessageEvent tEvent = new TimedMessageEvent(msg, System.nanoTime());
//        emit(tEvent);
//    }
//
//    private void cleanUp() {
//        emit(new TimedMessageEvent("eof", System.nanoTime()));
//    }
//
//    private class WikipediaEventHandler implements EventHandler {
//        @Override
//        public void onOpen() throws Exception {}
//
//        @Override
//        public void onClosed() throws Exception {
//            cleanUp();
//        }
//
//        @Override
//        public void onMessage(String s, MessageEvent e) throws Exception {
//            processMessage(e);
//        }
//
//        @Override
//        public void onComment(String s) throws Exception {}
//
//        @Override
//        public void onError(Throwable throwable) {}
//    }
//
//    // Unit Test
//    public static void main(String[] args) {
//        ConcurrentLinkedQueue<TimedMessageEvent> output = new ConcurrentLinkedQueue<>();
//        WikipediaSourceOperator source = new WikipediaSourceOperator("https://stream.wikimedia.org/v2/stream/recentchange", 10, output);
//        source.start();
//        try {
//            source.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Output size: " + output.size());
//    }
//}
