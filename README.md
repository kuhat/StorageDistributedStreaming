# A Stream Processing System with State Disaggregation

This is a simple Java-based stream system that enables disaggregate computation and state storage.

## Intro

In this system, we implemented a simple word count application to demostrate the disaggregation. It consists of DAG engine that controls the workflow of the system, including migration strategy implementation, and Nodes that are initialized as workers, Control Plane or Reconfiguration Manager later. 

- Worker: Handles computation, state update, migration and rate control, it is splitted into three kinds. Each operator is extended from an [Operator](./dataflow/src/main/java/operators/Operator/java) abstract class.
  - [Source](./dataflow/src/main/java/operators/FileSourceOperator.java): Reads word stream from a file, split sentences into single words and pass them to the downstream workers 
  - [Count](./dataflow/src/main/java/operators/CountOperator.java): Update the passed word's count and pass the updated result to downstream workers
  - [Sink](./dataflow/src/main/java/operators/SinkOperator.java): Store word count into log file and generate additional file for performance calculation
- [Control Plane](./dataflow/src/main/java/ControlPlane/ControlPlane.java): Generate & Update routing table seemlesslly and help workers find states storage
- Reconfiguration Manager: Handle reconfiguration

## How to extend

Here is an example of the guideline to this project. But you're encouraged to come up with a different design :)

- Modify it so that operators can be executed by different processes that may be local or distributed. 
  - For example, you may start a TaskManager process on each worker, run the tasks that are scheduled on this worker, and communicate with the control plane in order to know where to forward state/messages.
- Use a pub-sub system to serve as a control plane. Support scheduling between control plane and tasks.
- Support parallelism to enable scale up / scale down.
- Implement some basic operators (like data source, data sink, map, filter, window).
  - You can add more abstract class to abstract the common parts if necessary.
- Enable stateful operators, e.g. by connecting your system with a key-value store.
- Support event time and windows, if time allows. 

## How to build

1. Open Maven Tool View  
![View-Tool Windows-Maven](/docs/open_maven_view.png)
2. Reload source and generate java code defined in .proto  
![buttons to reload and generate code](/docs/code_generate_instructions.png)

## How to run
In IDEA IDE, navigate to 

>  ./dataflow/src/main/java/applications/WordCountApplication.java  

OR

> ./dataflow/src/main/java/applications/WordCountApplicationWithMigration.java

click 'run' button  
**Warning: the CPU and memory may reach 100% during execution**

## How to find worker process list
Windows:
> jps -v | C:\Windows\System32\findstr.exe /r /c:”SimpleWorker”

Unix:
> jps -v | grep SimpleWorker

## Check the result
After the log dislay 'SINK Data processed END_OF_FILE : 1': 

stop the program and check under directory `team-3b/dataflow`, two files are generated:
1. output.json: contains word count result
2. sinkOperator.log: contains the working log, and can be visualized by team-3b/dataflow/plot.py


### Example Result:
#### Experiment Environment:
CPU: AMD Ryzen 9 5900X 12-Core Processor
RAM: 32.0 GB
SSD: SAMSUNG SSD 970 EVO PLUS 1TB

#### Experiment Result:
![Result](/docs/no_migration_VS_PPSmigraion.png)
#### Experiment Analysis:
Default pipeline structure is composed of one source worker, two counter workers, one sink worker.  
The blue line shows the throughput vs time of default pipeline running without migration. Two counter workers are used from the beginning to the end.  
The orange line shows the result of application runs with pipeline stall migration during reconfiguration. One new counter worker 
is added during the reconfiguration(right after 20 seconds). The pipeline stalls during the reconfiguration.

During reconfiguration, orange line through put drops to 0. When reconfiguration is finished, the system resumes and the throughput is higher than
 before the reconfiguration, since more workers is sharing the task. 

Near the end of the job, both line drops when the source worker put the last data tuple into the input queue of each counter. Some counter  
finishes early, while other counters are still working to process all the data in the queue. Once all counter finish their job, the dataflow aggregating point - the
'SINK' - will experience throughput drop. 

The graph with disaggregate pipeline is still under construction.



## Evaluation
> How to evaluate the throughput?

We use a logger to record the process time of each tuple: when it arrives at the SinkOperator. For a more precise result, we record the system nano time. When the logger records the time, it will also dump the record to the log file. The default path of the result log file is located under the root folder of the project (`/team-3b/SinkOperator.log`).

The throughput graph can be plotted by running `plot.py` under `/team-3b/evaluation`. The throughput is calculated using the following equation:

$$ 
AverageThroughput=\frac{TupleNum}{interval/10^9}(events/s) 
$$

Tuplenum is the amount of the tuples arrived in a time interval, the default interval is 10^9 nano sec, which is 1 sec.

The plot script has two mode, `--mode=1` is used to display only one result, while `--mode=2` is used to compare the throughput from two different log files.

For example:

```sh
python plot.py --mode=1 --log_file=../../SinkOperator.log # display one result
python plot.py --mode=2 --log_file=SinkOperator1.log --log_file2=SinkOperator2.log
```

Please note that, we've added more features to our project since the demo day, so if you want to reproduce the result we showed on that day, you may have to checkout on the branch `tommzy/addWorkers` and reset your loacal branch to commit `73b32f7da8da9cf8cbc9c12b4183c1622b6c3317`
