package eu.phisikus.pivonia.tcp.utils;

import eu.phisikus.pivonia.pool.address.Address;
import io.vavr.control.Try;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * Provides network address from one of the network interfaces
 */
public class LocalAddressResolver implements NetworkAddressResolver {

    @Override
    public Try<Address> getAddress() {
        return getLocalInterfaceAddress()
                .flatMap(address -> AvailablePortProvider
                        .getRandomPort()
                        .map(port -> new Address(address, port))
                ).recoverWith(throwable -> Try.failure(new AddressNotResolvedException(throwable)));
    }

    private Try<String> getLocalInterfaceAddress() {
        return Try.of(NetworkInterface::networkInterfaces)
                .map(networkInterfaceStream -> networkInterfaceStream
                        .filter(nic -> Try.of(() -> !nic.isLoopback() && nic.isUp()).getOrElse(true))
                        .flatMap(NetworkInterface::inetAddresses)
                        .map(InetAddress::getHostAddress)
                        .findFirst()
                ).flatMap(firstAddress -> firstAddress
                        .map(Try::success)
                        .orElseGet(() -> Try.failure(new AddressNotResolvedException()))
                );
    }


}
