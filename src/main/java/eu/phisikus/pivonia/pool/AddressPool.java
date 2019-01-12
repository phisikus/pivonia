package eu.phisikus.pivonia.pool;

import io.reactivex.subjects.PublishSubject;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

class AddressPool {
    @Getter
    private Set<Address> addresses = new HashSet<>();

    @Getter
    private PublishSubject<Address> newAddresses = PublishSubject.create();


    public Address add(String hostname, int port) {
        var newAddress = new Address(hostname, port);
        addresses.add(newAddress);
        newAddresses.onNext(newAddress);
        return newAddress;
    }


    public void remove(Address address) {
        addresses.remove(address);

    }

}
