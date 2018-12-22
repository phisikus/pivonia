package eu.phisikus.pivonia;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerTestUtils {

    private static final String PORT_NOT_FOUND_MESSAGE = "Could not find a random port!";

    public static int getRandomPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(PORT_NOT_FOUND_MESSAGE, e);
        }
    }
}
