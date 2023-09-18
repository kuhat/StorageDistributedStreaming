package backends.migration;

import backends.DAG.CompleteDAG;
import backends.engines.DAGEngine;
import backends.engines.MultilayerDAGEngine;
import backends.migration.strategy.DisaggregationMigrationManager;
import backends.migration.strategy.MigrationManager;
import backends.migration.strategy.PipelineStallMigrationManager;
import utils.MigrationMethod;

public class MigrationManagerFactory {

    public static MigrationManager create(MigrationMethod method, MultilayerDAGEngine engine) {
        switch (method) {
            case PIPELINESTALLING:
                return new PipelineStallMigrationManager(engine);
            case DISAGGREGATION:
                return new DisaggregationMigrationManager(engine);
            default:
                return null;
        }
    }
}
