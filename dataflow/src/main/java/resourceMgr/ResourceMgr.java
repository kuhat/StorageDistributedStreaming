package resourceMgr;

import operators.CountOperator;
import operators.Operator;
import org.apache.flink.shaded.guava30.com.google.common.graph.Network;
import utils.NetworkAddress;
import utils.PortManager;
import workers.Worker;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @projectName: team-3b
 * @package: resourceMgr
 * @className: ResourceMgr
 * @author: Danny
 * @date: 2023/3/14 15:14
 * @version: 1.0
 */
public class ResourceMgr {
    private List<Operator> operators;
    private Timer timer;
    private reconfigClient client;
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private static final String IP_ADDRESS = "localhost";
    private NetworkAddress localAddress, engineAddress;
    private UUID id;

    public ResourceMgr(NetworkAddress engineAddress){
        logger.log(Level.INFO, "Resource Manager client started at: " + engineAddress.toString());
        this.timer = new Timer();
        operators = new ArrayList<>();
        this.engineAddress = engineAddress;
        localAddress = new NetworkAddress(IP_ADDRESS, PortManager.getFreePort());
        id = UUID.randomUUID();
        init();
    }

    private void init() {
        // Initialize countOperator1, 2, destination
        client = new reconfigClient(id, UUID.randomUUID(), localAddress, engineAddress);
    }

    // start the timer, every 20 seconds run a reconfiguration
    private void start() {
        logger.log(Level.INFO, "Reconfiguration manager client start...............");
        timer.schedule(new TimerTask() {
            public void run() {
                System.out.println("-------start reconfig--------");
                String res =  client.Reconfig("1");
                System.out.println("res from service: " + res);
            }
        }, 20000); // 20000 Set after 20s, run the first operator addition
    }

    public void startNewWorker() {
        CountOperator newOperator = new CountOperator();
        addWorker(newOperator);
        String confirmation = this.client.Reconfig("1");
        logger.log(Level.INFO, "confirmation receive: " + confirmation);
    }

    public void addWorker(Operator worker) {
        this.operators.add(worker);
    }

    public static void main(String[] args) {
        String server = args[0];
        String port = args[1];
        NetworkAddress engineAddress = new NetworkAddress(server, Integer.valueOf(port));
        ResourceMgr resourceMgr = new ResourceMgr(engineAddress); // TODO: fill in engine address
        resourceMgr.start();
    }

}
