package rpc.output;

import utils.NetworkAddress;

import java.util.UUID;
import java.util.logging.Logger;

public abstract class OutputClient {
    private Logger logger;
    private NetworkAddress hostAddress;
    private NetworkAddress targetAddress;
    private UUID hostID;
    private UUID clientID;

    public OutputClient(UUID hostId, UUID targetID, NetworkAddress hostAddress, NetworkAddress targetAddress) {
        setHostId(hostId);
        setClientID(targetID);
        setTargetAddress(targetAddress);
        setHostAddress(hostAddress);
    }

    public NetworkAddress getHostAddress() {
        return hostAddress;
    }
    public NetworkAddress getTargetAddress() {
        return targetAddress;
    }
    public void setHostAddress(NetworkAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    public void setTargetAddress(NetworkAddress targetAddress) {
        this.targetAddress = targetAddress;
    }

    public UUID getClientID() {
        return clientID;
    }

    public void setClientID(UUID clientID) {
        this.clientID = clientID;
    }
    public UUID getHostUUId() {
        return hostID;
    }

    public void setHostId(UUID id) {
        this.hostID = id;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
