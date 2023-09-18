package backends.DAG;
import backends.communication.output.OutputCommandClient;
import operators.OperatorType;
import utils.NetworkAddress;
import workers.Worker;

import java.util.UUID;

public class WorkerInfo {
    private NetworkAddress address;
    private OperatorType operatorType;
    private OutputCommandClient outputCommandClient;
    private UUID id;

    public WorkerInfo(OperatorType operatorType)
    {
        setOperatorType(operatorType);
    }
    public WorkerInfo(NetworkAddress address, OperatorType operatorType, UUID id) {
        this(operatorType);
        setAddress(address);
        setId(id);
    }
    public WorkerInfo(NetworkAddress address, OperatorType operatorType, UUID id, OutputCommandClient outputCommandClient) {
       this(address, operatorType, id);
       setOutputCommandClient(outputCommandClient);
    }

    public boolean isInfoFullFilled()
    {
        return address != null && operatorType != null;
    }

    public String toString()
    {
            return String.format("WorkerInfo: address: %s, operatorType: %s, id: %s", address, operatorType, id);
    }

    public boolean equals(Object anotherWorker)
    {
        if(!(anotherWorker instanceof WorkerInfo))
        {
            return false;
        }
        return ((WorkerInfo)anotherWorker).getAddress().equals(this.getAddress()) &&
                ((WorkerInfo)anotherWorker).getOperatorType().equals(this.getOperatorType());
    }

    // getters and setters
    public NetworkAddress getAddress() {
        return address;
    }

    public void setAddress(NetworkAddress address) {
        this.address = address;
    }

    public String getDomain() {
        return address.getDomain();
    }

    public int getPort() {
        return address.getPort();
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(OperatorType operatorType) {
        this.operatorType = operatorType;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OutputCommandClient getOutputCommandClient() {
        return outputCommandClient;
    }

    public void setOutputCommandClient(OutputCommandClient outputCommandClient) {
        this.outputCommandClient = outputCommandClient;
    }

//    public int hashCode()
//    {
//        int addressHash = address == null ? 0 : address.hashCode();
//        int operatorTypeHash = operatorType == null ? 0 : operatorType.hashCode();
//        int idHash = id == null ? 0 : id.hashCode();
//        return addressHash + operatorTypeHash + idHash;
//    }

}
