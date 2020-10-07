package eu.phisikus.pivonia.test;

import eu.phisikus.pivonia.tcp.utils.LocalAddressResolver;
import eu.phisikus.pivonia.tcp.utils.NetworkAddressResolver;

public class ServerTestUtils {
    private static final NetworkAddressResolver LOCAL_ADDRESS_RESOLVER = new LocalAddressResolver();
    public static int getRandomPort() {
        return LOCAL_ADDRESS_RESOLVER
                .getAddress()
                .get()
                .getPort();
    }
}
