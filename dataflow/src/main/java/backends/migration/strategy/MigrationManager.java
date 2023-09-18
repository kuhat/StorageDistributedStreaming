package backends.migration.strategy;

import backends.DAG.CompleteDAG;
import backends.DAG.NodeInfo;
import backends.communication.input.InputCommandServer;
import backends.communication.output.OutputCommandClient;
import backends.engines.DAGEngine;
import backends.engines.MultilayerDAGEngine;
import com.esotericsoftware.kryo.io.Output;
import utils.NetworkAddress;
import utils.ScaleDirection;
import workers.router.RoutingTable;
import workers.storage.KeyRange;

import java.util.logging.Logger;

/**
 * Migration Manager
 * Identify which node need to change router and which node need to migrate data
 *
 */
public abstract class MigrationManager {
    private MultilayerDAGEngine engine;
    private Logger logger;

    public MigrationManager(MultilayerDAGEngine engine)
    {
        setEngine(engine);
    }

    public abstract void performStateMigration(KeyRange keyRange, NetworkAddress destAddress);
    public abstract void performRouterChange(RoutingTable table);
    public abstract void upScale(int layerIndex);
    public abstract void downScale(int layerIndex);

    // getters and setters
    public MultilayerDAGEngine getEngine() {
        return engine;
    }

    public void setEngine(MultilayerDAGEngine engine) {
        this.engine = engine;
    }
    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
