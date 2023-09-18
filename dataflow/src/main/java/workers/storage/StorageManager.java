package workers.storage;

import operators.OperatorType;
import utils.NetworkAddress;
import utils.PortManager;
import utils.StorageType;
import workers.migration.communication.StateReceivingServer;
import workers.migration.communication.StateSendingManager;


public class StorageManager<K, V> {

    private static final String DB_DIR_PATH = "worker_storage/";
    private IKVStorage<K, V> storage;
    private StateReceivingServer stateReceivingServerServer;
    private StateSendingManager stateSendingManager;

    public StorageManager(OperatorType type, StorageType storageType, String domain, int port) {
        switch (storageType) {
            case HEAP:
                storage = new HeapKVStorage<>();
                break;
            case ROCKSDB:
                storage = new RocksKVStorage<>(DB_DIR_PATH + domain + port + ".db");
                break;
        }
        setStateReceivingServerServer(new StateReceivingServer(type, new NetworkAddress(domain, PortManager.getFreePort()), storage));
        setStateSendingManager(new StateSendingManager());
    }

    public void migrateData(KeyRange keyRange, NetworkAddress destAddress) {
        // TODO: implement migrateData method

    }

    public void fetchData() {
        // TODO: implement fetchData method

    }

    // getters and setters
    public IKVStorage<K, V> getStorage() {
        return storage;
    }

    public void setStorage(IKVStorage<K, V> storage) {
        this.storage = storage;
    }

    public StateReceivingServer getStateReceivingServerServer() {
        return stateReceivingServerServer;
    }

    public void setStateReceivingServerServer(StateReceivingServer stateReceivingServerServer) {
        this.stateReceivingServerServer = stateReceivingServerServer;
    }

    public StateSendingManager getStateSendingManager() {
        return stateSendingManager;
    }

    public void setStateSendingManager(StateSendingManager stateSendingManager) {
        this.stateSendingManager = stateSendingManager;
    }
}
