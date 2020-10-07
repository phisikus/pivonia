package eu.phisikus.pivonia.tcp.utils;

import io.vavr.control.Try;

import java.net.ServerSocket;

public class AvailablePortProvider {

    /**
     * Provides port number currently available for use.
     *
     * @return available port
     */
    public static Try<Integer> getRandomPort() {
        return Try.withResources(() -> new ServerSocket(0))
                .of(serverSocket -> {
                    serverSocket.setReuseAddress(true);
                    return serverSocket.getLocalPort();
                });
    }
}
