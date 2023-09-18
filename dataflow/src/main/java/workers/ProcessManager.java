package workers;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * ProcessManager
 * 1. manage all the processes
 * 2. terminate all the processes
 */
public final class ProcessManager extends Thread{

    private List<Process> processes;
    private List<Thread> waitingThreads;
    private boolean isRunning = true;

    /**
     *
     * constructor
     */
    public ProcessManager()
    {
        this.processes = new LinkedList<>();
        this.waitingThreads = new LinkedList<>();
    }

    /**
     * wait for all the processes to finish
     */
    public void run()
    {
        while(isRunning)
        {
            for(Thread t : waitingThreads)
            {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    /**
     * create a process builder
     * @param clazz
     * @param args
     * @return
     */
    private ProcessBuilder createBuilder(Class clazz, List<String> args)
    {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String processTitle = "-Doperator=" + args.get(2) + "@" + args.get(0) + ":" +args.get(1);
        String className = clazz.getName();

        List<String> command = new LinkedList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(classpath);
        command.add(processTitle);
        command.add(className);
        if (args != null) {
            command.addAll(args);
        }
        return new ProcessBuilder(command);
    }

    /**
     * instantiate a new worker process
     * @param clazz
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public void exec(Class clazz, List<String> args) throws IOException,
            InterruptedException {

        ProcessBuilder builder = createBuilder(clazz, args);
        Process process = builder.inheritIO().start();
        this.processes.add(process);
        Thread t = new Thread(() -> {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                // handle interruption
            }
        });
        t.start();
        waitingThreads.add(t);
    }

    /**
     * terminate all the processes
     */
    public void terminateAll()
    {
        for(Process p : processes)
        {
            p.destroy();
        }
    }

    public List<Process> getProcesses() {
        return this.processes;
    }
}