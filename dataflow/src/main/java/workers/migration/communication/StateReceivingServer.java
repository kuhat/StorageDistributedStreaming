package workers.migration.communication;

import rpc.input.OperatorInputServer;
import operators.OperatorType;
import utils.NetworkAddress;
import workers.storage.IKVStorage;

import java.io.IOException;
import java.util.logging.Logger;

public class StateReceivingServer extends OperatorInputServer {

    public StateReceivingServer(OperatorType type, NetworkAddress serverAddress, IKVStorage storage){
        super(type, serverAddress);
        setServer(getServerBuilder().addService(new StateReceivingService(storage, (int size)->{onReceive(size);}))
                .build());
        setLogger(Logger.getLogger(this.getClass().getName()));
    }
    @Override
    public void startServer() throws IOException {
        getServer().start();
        getLogger().info("~~~~ " + getOperatorType() + ":" + getPort() +  " State Receiving Server started ~~~~");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("~~~ " + StateReceivingServer.this.getOperatorType() + ":" + getPort() +
                        " shutting down State Receiving Server");
                try {
                    StateReceivingServer.this.stopServer();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("~~~~ " + StateReceivingServer.this.getOperatorType() + ":" + getPort() +
                        " State Receiving Server Shut Down.");
            }
        });
    }

    private void onReceive(int size)
    {
        getLogger().info("~~~~~STATE MIGRATION~~~~:" + getOperatorType() + "@" + getServerAddress() +  " received " + size + " States ~~~~");
    }
}
