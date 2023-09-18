package rpc.input;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import utils.NetworkAddress;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class InputServer extends Thread{
    private Logger logger;
    protected static final int TIMEOUT = 10;

    private NetworkAddress serverAddress;

    /**
     * The gRPC server.
     */
    private Server server;
    private ServerBuilder<?> serverBuilder;

    public InputServer(NetworkAddress serverAddress)
    {
        this(Grpc.newServerBuilderForPort(serverAddress.getPort(), InsecureServerCredentials.create()),
                serverAddress);
    }
    public InputServer(ServerBuilder<?> serverBuilder, NetworkAddress serverAddress)
    {
        setServerAddress(serverAddress);
        this.serverBuilder = serverBuilder;
    }

    public void run()
    {
        try
        {
            this.startServer();
            this.blockUntilShutdown();
        } catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    public abstract void startServer() throws IOException;

    /** Stop serving requests and shutdown resources. */
    public void stopServer() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * getters and setters
     */

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public NetworkAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(NetworkAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getPort() {
        return getServerAddress().getPort();
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public ServerBuilder<?> getServerBuilder() {
        return serverBuilder;
    }

    public void setServerBuilder(ServerBuilder<?> serverBuilder) {
        this.serverBuilder = serverBuilder;
    }
}
