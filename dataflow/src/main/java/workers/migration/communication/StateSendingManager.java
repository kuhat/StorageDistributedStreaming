package workers.migration.communication;

import utils.IterableMapCollection;
import workers.router.Router;

import java.util.logging.Logger;

// TODO: implement StateReceivingServer class
public class StateSendingManager extends Thread{
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private IterableMapCollection<StateSendingClient> stateMigrationCollection;
    private Router router;
    private String hostAddress;


}
