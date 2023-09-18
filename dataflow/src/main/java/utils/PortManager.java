package utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A helper class to get a free port
 */
public class PortManager {

    public static String getIPAddress()
    {
        try ( Socket socket = new Socket()){
            socket.connect(new InetSocketAddress("google.com", 80));
            return socket.getLocalAddress().getHostAddress();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    /**
     * Get a free port
     * @return a port that available
     */
    public static int getFreePort() {
        int port = 0;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        } catch (IOException e) {
            // handle the exception
        }
        return port;
    }
}
