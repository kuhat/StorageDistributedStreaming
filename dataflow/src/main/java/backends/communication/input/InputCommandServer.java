package backends.communication.input;

import backends.IDAGReconfigCallback;
import rpc.input.InputServer;
import utils.NetworkAddress;

import java.io.IOException;
import java.util.logging.Logger;

public class InputCommandServer extends InputServer {
    public InputCommandServer(NetworkAddress serverAddress, IDAGReconfigCallback reconfigCallback)
    {
        super(serverAddress);
        setServer(getServerBuilder().addService(new InputCommandService(reconfigCallback))
                .build());
        setLogger(Logger.getLogger(this.getClass().getName()));
    }

    /**
     * Start serving requests.
     * @throws IOException
     */
    public void startServer() throws IOException {
        getServer().start();
        getLogger().info("<<<< DAGengine Re-config notification receiver:" + getPort() +  " started <<<<");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** Re-config notification receiver:" + getPort() + " shutting down");
                try {
                    InputCommandServer.this.stopServer();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("<<<< Re-config notification receiver:" + getPort() + " Shut Down.");
            }
        });
    }
}
