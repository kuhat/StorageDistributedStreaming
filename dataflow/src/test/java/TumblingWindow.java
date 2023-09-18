import org.checkerframework.checker.units.qual.C;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TumblingWindow {
    private final int MAX_BATCH_SIZE = 10;
    private final int timeoutMilliSec = 10000;
    private int num = 0;
    private Timmer timmer;
    public TumblingWindow()
    {
        timmer = new Timmer(timeoutMilliSec);
        timmer.start();
//        try {
//            timmer.join();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    public void fireDataBatchDownFlow()
    {
        num++;
        if(num % MAX_BATCH_SIZE == 0 && num != 0)
        {
            synchronized (timmer) {
                timmer.notify();
            }
        }
    }
    public static void main(String[] args) {
        TumblingWindow tw = new TumblingWindow();
        for (int i = 0; i < 100; i++) {
            tw.fireDataBatchDownFlow();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class Timmer extends Thread{
        private int timeoutMilliSec;
        private boolean isRunning = true;
        public Timmer(int timeoutMilliSec)
        {
            this.timeoutMilliSec = timeoutMilliSec;
        }

        @Override
        public void run() {
            while(isRunning)
            {
                synchronized (this)
                {
                    try {
                        wait(timeoutMilliSec);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("num: " + num);
                }
            }
        }

        public void stopTimmer()
        {
            isRunning = false;
        }
    }
}
