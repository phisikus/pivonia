package eu.phisikus.pivonia.tcp.utils;

import eu.phisikus.pivonia.pool.address.Address;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Resolves network address using predefined value
 */
@AllArgsConstructor
public class FixedAddressResolver implements NetworkAddressResolver {

    @NonNull
    private final Address address;

    @Override
    public Try<Address> getAddress() {
        return Try.success(address);
    }
}
