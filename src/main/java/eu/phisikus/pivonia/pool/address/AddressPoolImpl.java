package eu.phisikus.pivonia.pool.address;

import eu.phisikus.pivonia.pool.AddressPool;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class AddressPoolImpl implements AddressPool {

    private Set<Address> addresses = new HashSet<>();

    private Subject<AddressEvent> addressChanges = PublishSubject.create();

    public Address add(String hostname, int port) {
        var newAddress = new Address(hostname, port);
        var changeEvent = new AddressEvent(AddressEvent.Operation.ADD, newAddress);
        addresses.add(newAddress);
        addressChanges.onNext(changeEvent);
        return newAddress;
    }

    public void remove(Address address) {
        var changeEvent = new AddressEvent(AddressEvent.Operation.REMOVE, address);
        addresses.remove(address);
        addressChanges.onNext(changeEvent);
    }

    @Override
    public List<Address> getAddresses() {
        return new LinkedList<>(addresses);
    }

    @Override
    public Observable<AddressEvent> getAddressChanges() {
        return addressChanges;
    }

}
