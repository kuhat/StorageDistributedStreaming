package ControlPlane;

import rpc.input.InputServer;
import utils.NetworkAddress;

import java.io.IOException;
import java.util.logging.Logger;

public class ControlPlaneServer extends InputServer {

    public ControlPlaneServer(NetworkAddress address, RemoveWorkerCallback removeWorkerCallback, AddWorkerCallback addWorkerCallback
    , RoutingTableCallback routingTableCallback, GetStateCallback getStateCallback){
        super(address);
        setServer(getServerBuilder().addService(new ControlPlaneService(removeWorkerCallback,addWorkerCallback,routingTableCallback,getStateCallback))
                .build());
        setLogger(Logger.getLogger(this.getClass().getName()));
    }
    @Override
    public void startServer() throws IOException {
        getServer().start();
        getLogger().info("<<<< ControPlane notification receiver:" + getPort() +  " started <<<<");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** ControlPlane notification receiver:" + getPort() + " shutting down");
                try {
                    ControlPlaneServer.this.stopServer();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("<<<< ControlPlane notification receiver:" + getPort() + " Shut Down.");
            }
        });
    }
}
