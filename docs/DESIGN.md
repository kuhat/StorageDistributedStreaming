# Design Document

### Project 3 team B

## Problem Statement

### Problem Addressing

The project is addressing the problem of high latency and downtime during the migration of stateful streaming dataflow
operators. Stream processing systems reconfiguration is expensive. When a Task Manager reconfiguration is necessary, data flow stops
until Task Manager state migration is finished.

The reconfiguration in this project includes increasing the number of Task Managers, or removing existing Task Manager(s).
The proposed solution aims to disaggregate computation and storage, allowing for a standalone control plane that serves
as a routing table for state access to these tasks.

### Importance

This is an important problem to solve because the high latency and downtime during state migration can impact the performance
of stream processing systems, which are increasingly used in various industries, such as finance, telecommunications,
and e-commerce. By disaggregating computation and storage, and introducing a control plane, stream processing systems can
be more flexible and efficient, allowing for faster state migration and reduced downtime.

### Benefits

The beneficiaries of this project are organizations and businesses that rely on stream processing systems to handle
large volumes of real-time data, as they will be able to improve the efficiency and performance of their data
processing, leading to faster and more accurate decision-making. Additionally, developers and data engineers who work
with stream processing systems will benefit from a more flexible and efficient architecture, allowing for faster
development and deployment of data pipelines.

## Proposed Solution

The solution is to have a third-party component, the Control Plane, to monitor and keep track of workers and their
states in each Task Managers. Task Managers can communicate with each other for operator states queries.
When a reconfiguration is required, newly added Task Manager can pull from the Control Plane its state address. As data flows
continue, the new Task Manager should no longer require state access by querying them from another Task Manager(s).

### Control Plane: distributable, replicable and fault-tolerant

- Map each operator to its state location. Answer worker's question "where's my state"
- Maintain a Consistent Hash Ring
- Repartition Consistent Hash Ring when new worker added
- Keep track of the state migration progress, which worker is under migration, which one already finished.
- Phase 0 - implementing a Consistent Hash Ring in memory. it can be started by creating a ConsistentHash Class in control plane with `get()`, `add()`, `replace()` functions. After the entire system framework is up, we can consider using ETCD (key-value store) for handle system failure.  
  `Implementation-wise, encapsulation of the storage part is necessary for further extension.`
- Apache Kafka can be used to serve as the control plane. The following key features make it well-suited for this architecture:
  1. Decoupled Architecture: Apache Kafka provides a decoupled architecture in which the producers and consumers are separated
     from each other without noticing the states of each other.
  2. Publish-Subscribe architecture: Kafka uses a Pub-Sub model, where tasks can subscribe to the desired topics and
     receive messages as they are produced.
  3. Scalability: Kafka is highly scalable, thus it can be scaled up or down horizontally and can be deployed in a
     distributed manner.
  4. Durability: the durable message storage in Kafka ensures that the messages in the storage are not lost in the event
     of a node failure. Kafka is perfect to serve as the control plain, where metadata and routing information can be reliably
     stored and available whenever a task manager queries the information.

### Resource Manager

- Add/delete Task Managers as necessary.
- Assign tasks to Task Managers.
- Monitoring the heartbeats of each Task Manager.
- Monitory overall and individual Task Manager resource status (CPU, memory usage).
- Hot-swappable strategy between 1. Migration state with whole pipeline stall and 2. Migration states dynamically.

### Task Managers: distributable, fault-tolerant, and stateful (RockDB)

- Maintain and execute operators in Task Slots.
- Configurable to force local resource access only, or both remote and local.
- Able to migrate state to/from another Task Manager.
- Access state that is located in the current entity, and state that is located in other Task Managers.
- Hot-swappable strategies between 1. Migration state with whole pipeline stall and 2. Migration states dynamically.
- Share states with other Task Managers.
- Report resource usage status to Control Plane.  
  `Implementation: Composed by two queues, one for input and one for output.`

### Operators

- source - read in data from a local file or online source
- sink - can feed the latency data into another app that plots the latency graph
- map
- filter
- window
- join

### Functions

- Watermark

`Put them in a subfolder together.`

### Applications

- Phase 0 - create a word count application that counts the word appearance form a text file.
- Phase 1+ (After state migration can be performed dynamicly)Create multiple applications with different operators, and write tests.
- Take inputs from the local storage or an online source.
- Config and execute the pipeline through Control Plane.

### Design Choices

1. When state changes, should Task Managers ask Control Plane first or look into local first?
   _ Depends on the balance between the overhead of the Control Plane lookup and the potential benefit of finding the
   state faster.
   If the state is likely to be stored locally, it is more efficient to do the local lookup locally first.
   On the other hand, if the state is not stored locally, then the local lookup will waste time vs do the remote
   lookup at the Control Plane directly.
   _ We can design a decision strategy that utilizes a buffer to store the request history of the Task Manager, and
   modify the decision dynamically. \* If a state has been requested recently, then more likely it is stored locally already. In such case, do the lookup
   locally first would be more efficient. If the state is not requested, then do the remote lookup directly at the
   Control Plane would be efficient.

2. When the migration finishes, should we keep the connection between Task Manager A2 to A1?
   _ This depends on the likelihood of further state access and the cost of maintaining the connection.
   _ If the state is unlikely to be needed again or the cost of maintaining the connection is high, it may be better to close the
   connection. \* On the other hand, if the state is likely to be needed again or the cost of maintaining the connection
   is low, it may be better to keep the connection and avoid the overhead of establishing a new one. This also requires
   a simulation experiment to see which one is better.

## Expectations

What we measure:  
**Latency = state change time - arriving time**  
**State change time: E.G time of word counter changed**

1. Average latency BEFORE system doing migration
2. Average latency DURING migration, due to adding a worker / replacing a worker
3. Average latency AFTER system doing migration
4. Average latency within WHOLE system processing. (which means we have a fixed amount of data for experiment)
5. States migration duration
6. Total processing time for entire data set(with the same speed settings)

What we compare(designs that we compare):

1. A system that stalls the pipeline when migration is necessary
2. Systems that can query its states from remote.

What we expect:  
Abstract:

1. Latency BEFORE migration, should be the same between design 1 and 2.
2. Latency DURING migration, design 1 latency would be the amount of time that required for migration, design 2 latency would be the smaller than that of design 1.
3. Latency AFTER migration. If the migrated state operator is the bottom neck, then design 2 latency should lower thant design 1 after adding/replacing worker. If state operation is not the bottom neck, latency should should be the same between design 1 and 2.

Details:
| Item | Design 1 vs Design 2 | Note |
| ----------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BEFORE migration latency | similar latency | |
| DURING migration latency | Design 1 > Design 2 latency-wise | design 1 latency would be <br /> the amount of time that required for migration <br /> design 2 latency would be <br /> the smaller than that of design 1 |
| AFTER migration latency | If the migrated state operator is the bottom neck, <br /> Design 1 > Design 2 latency-wise. <br /> If not, Design 1 = Design 2 latency-wise | |
| total processing time | Design 1 > Design 2 | It depends on <br /> 1. size of testing dataset <br /> 2. total running time of the trial<br /> 3. number of times that reconfiguration happens |
| Average latency for the WHOLE system processing | we hope Design 1 > Design 2 | It depends on <br /> 1. size of testing dataset <br /> 2. total running time of the trial<br /> 3. number of times that reconfiguration happens |
| States migration duration | Design 1 < Design 2 | |
| Total processing time | Design 1 > Design 2 | It depends on <br /> 1. size of testing dataset <br /> 2. total running time of the trial<br /> 3. number of times that reconfiguration happens |

What we need to be aware of:

1. What is network transfer overhead?
2. What is RocksDB read/write overhead during migration? what is the best solution?
3. When or how the benefits of state disaggregate leveraged? More often data migration?
4. What is the best solution of migration, keep a snap shoot of the states that will be migrated at prime worker？Passively query? When data is incoming, update both?

## Experimental Plan

### Definition

**Latency = state change time - arriving time**

### Data Source

| Data Source Type | Name                                             | Size       |
| ---------------- | ------------------------------------------------ | ---------- |
| Text File        | The Complete Works of <br /> William Shakespeare | 950k Words |

### Experimental Setup

Run data flow twice with the same source and same configuration(speed / rate) but 2 different Task Manager/Control
Plane configurations.

- Trial #1 config:
  Set Task Manager/Control Plane strategy to migration state with whole pipeline stall

- Trial #2 config:
  Set Task Manager/Control Plane strategy to migration states dynamically

### Experimental Steps

Create a 'Word Count' Application that count the word show-up times in a steam.
When data arrive source, add a time stamp mark for it's arriving time.
When data is processed and word count changes, time stamp it again and calculate the latency of state change.

1. Start data aggregation application. Calculate average latency BEFORE reconfiguration (state change time - arriving time)
2. Trigger reconfiguration. Calculate latency DURING reconfiguration
3. Calculate average latency AFTER reconfiguration
4. Calculate the total time consumed.
5. Maybe repeat step 2-3 multiple times for creating the scenario that multiple reconfigurtions are needed.

### Experimental Data output

We can write the calculated latency data to sink and run another program to do windowing aggregation based on the migration status (BEFORE, DURING, AFTER migration) and come up with a chart.

### Experiment results display

Draft bar chart with latency & reconfiguration times.
Plot latency, average latency over time.

## Success Indicators

### Phase 0:

1. Create necessary classes.
2. Can read data from a text file to perform word count.
3. Modify existing sample application to use Control Plane/Task Manager instead of Operator directly.

**_Success means the existing sample data flow processing pipeline has the control plane / Task Manager introduced._**

### Phase 1:

1. Enable Control Plane operator/state mapping.
2. Implement remote state access.

**_Success means the current system can dynamically add/replace Task Managers without downtime._**

### Phase 2:

1. reduce state query time.

**_Success means the latency during reconfiguration decrease._**

**_Overall: success means stream processing system can add/replace Task Managers without stream stop, with migration latency generated as minimum as possible._**

## Task assignment

### Team skill matrix:

| Group Member   | Java | Algorithm | System design | Note(Strong ; need collaboration)                                                  |
| -------------- | ---- | --------- | ------------- | ---------------------------------------------------------------------------------- |
| Hui Zheng      | 7    | 7         | 0             | Java <br /> but lack Backend application / System design exp                       |
| Zeyu Su        | 4    | 1         | 0             | Good Java programming experience                                                   |
| Wenhao Zhou    | 4    | 3         | 1             | Rich Java development experiences<br /> but lack streaming application experiences |
| Richard Wei    | 5    | 4         | 2             | Java programming experience, pub/sub sys dev experience                            |
| Kangning Zhang | 2.5  | 2         | 1             | good with java programming<br /> but lack system design experience                 |

### Tasks List:

Element implementations:

1. App
2. Opeartors: 1.Source 2.Map 3.Sink 4.Filter 5.Window 6.Join
3. TaskManager/ Workder
4. Control Plane
5. Resource Manager

2-5 are independent. However, for developers hand on 3,4,5 should work together. Operators implementations are independent of each other.
App(#1) is based on 2-5.

For Phase 0, we can implement a word count app with a design that data migration will stall the pipeline.  
After the app is runnable, each of us can come up with our own idea about how data migrations could be designed.

### Task Assign:

| Group Member   | Main Module         | Specified Tasks |
| -------------- | ------------------- | --------------- |
| Hui Zheng      | TaskManager/Woker   |                 |
| Zeyu Su        | Implement operators |                 |
| Wenhao Zhou    | Resource Manager    |                 |
| Richard Wei    | Implement operators |                 |
| Kangning Zhang | Control Plane       |                 |
