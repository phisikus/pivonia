package eu.phisikus.pivonia.pool;

import io.vavr.collection.List;
import lombok.Getter;

class AddressPool {
    @Getter
    private List<Address> addresses = List.empty();

    public void add(String hostname, int port) {
        addresses = addresses.push(new Address(hostname, port));
    }

}
