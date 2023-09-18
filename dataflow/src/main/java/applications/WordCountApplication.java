package applications;

import java.util.ArrayList;
import java.util.List;

import backends.engines.MultilayerDAGEngine;
import operators.OperatorType;
import utils.MigrationMethod;
import utils.OperationLayer;

/**
 * Application layer
 * Word count application
 */
public class WordCountApplication {

    /**
     * main entrance of the application
     * create layers by defining operator count and type
     * select a processing engine to start
     * @param args
     */
    public static void main(String[] args) {
        List<OperationLayer> ol = new ArrayList<OperationLayer>();
        ol.add(new OperationLayer(1, OperatorType.SOURCE));
        ol.add(new OperationLayer(2, OperatorType.COUNT));
        ol.add(new OperationLayer(1, OperatorType.SINK));
        MultilayerDAGEngine engine = new MultilayerDAGEngine(ol, true, MigrationMethod.PIPELINESTALLING);
        engine.start();
    }
}
