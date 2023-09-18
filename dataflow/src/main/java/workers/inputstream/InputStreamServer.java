package workers.inputstream;

import rpc.input.OperatorInputServer;
import operators.OperatorType;
import utils.NetworkAddress;
import workers.*;
import workers.buffer.LocalBuffer;

import java.io.IOException;
import java.util.logging.Logger;
/**
 * input edge
 *
 * Input Stream Server for data flow
 *
 * @author
 *
 */
public class InputStreamServer extends OperatorInputServer {
    /**
     * Constructor
     *
     * Create a RouteGuide server using serverBuilder as a base and features as data.
     */
    public InputStreamServer(OperatorType type,
                             NetworkAddress serverAddress,
                             LocalBuffer<DataTuple> localBuffer,
                             StreamOperationSwitchCallBack streamOperationSwitchCallBack,
                             StreamRouterUpdateCallBack streamRouterUpdateCallBack,
                             StreamStateMigrationCallBack streamStateMigrationCallBack,
                             StreamMergeStateCallBack streamMergeStateCallBack,
                             StreamDrainCallBack streamDrainCallBack) {
        super(type, serverAddress);
        InputStreamOnReceiveCallback streamOnReceiveCallback = (int size) -> {this.onReceive(size);};
        setServer(getServerBuilder().addService(new InputStreamService(localBuffer,
                        streamOnReceiveCallback,
                        streamOperationSwitchCallBack,
                        streamRouterUpdateCallBack,
                        streamStateMigrationCallBack,
                        streamMergeStateCallBack,
                        streamDrainCallBack))
                .build());
        setLogger(Logger.getLogger(this.getClass().getName()));
    }

    /**
     * Start serving requests.
     * @throws IOException
     */
    public void startServer() throws IOException {
        getServer().start();
        getLogger().info("<<<< " + getOperatorType() + ":" + getPort() +  " Input Stream Server started <<<<");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** " + InputStreamServer.this.getOperatorType() + ":" + getPort() + " shutting down Input Stream Server");
                try {
                    InputStreamServer.this.stopServer();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("<<<< " + InputStreamServer.this.getOperatorType() + ":" + getPort() + " Input Stream Server Shut Down.");
            }
        });
    }

    private void onReceive(int size)
    {
        // TODO: uncomment this line for debugging
//            getLogger().info(getOperatorType() + "@" + getServerAddress() +  " received " + size + " tuples <<<<");
    }
}
