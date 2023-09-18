package utils;

// time window to send batched data down flow
public class Timer extends Thread{
    private int timeoutMilliSec;
    private boolean isRunning = true;
    private OnTimerCallBack onTimerCallBack;
    public Timer(int timeoutMilliSec, OnTimerCallBack onTimerCallBack)
    {
        this.timeoutMilliSec = timeoutMilliSec;
        this.onTimerCallBack = onTimerCallBack;
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
                // wait finished or interrupted(by notify(), which means 100 dataTuple collected)
                onTimerCallBack.onTimer();
            }
        }
    }
    public void stopTimer()
    {
        isRunning = false;
    }
}