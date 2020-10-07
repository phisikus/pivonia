package eu.phisikus.pivonia.tcp.utils;

import eu.phisikus.pivonia.pool.address.Address;
import io.vavr.control.Try;

public interface NetworkAddressResolver {
    /**
     * Provides machine address that can be used by other nodes to make connection.
     *
     * @return address under which current node should be reachable
     */
    Try<Address> getAddress();


    /**
     * Address could not be resolved by NetworkAddressResolver
     */
    class AddressNotResolvedException extends RuntimeException {
        public AddressNotResolvedException() {
        }

        public AddressNotResolvedException(Throwable cause) {
            super(cause);
        }
    }
}
