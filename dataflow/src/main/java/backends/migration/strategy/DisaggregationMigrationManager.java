package backends.migration.strategy;

import backends.DAG.CompleteDAG;
import backends.DAG.NodeInfo;
import backends.engines.DAGEngine;
import backends.engines.MultilayerDAGEngine;
import utils.NetworkAddress;
import utils.ScaleDirection;
import workers.router.RoutingTable;
import workers.storage.KeyRange;

public class DisaggregationMigrationManager extends MigrationManager {
    public DisaggregationMigrationManager(MultilayerDAGEngine engine) {
        super(engine);
    }

    @Override
    public void performStateMigration(KeyRange keyRange, NetworkAddress destAddress) {
    }

    @Override
    public void performRouterChange(RoutingTable table) {

    }

    @Override
    public void upScale(int layerIndex) {

    }

    @Override
    public void downScale(int layerIndex) {

    }
}
