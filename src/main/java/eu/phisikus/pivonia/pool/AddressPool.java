package eu.phisikus.pivonia.pool;

import eu.phisikus.pivonia.pool.address.Address;
import eu.phisikus.pivonia.pool.address.AddressChange;
import io.reactivex.Observable;

import java.util.List;

public interface AddressPool {

    /**
     * Add a new address to the address pool
     *
     * @param hostname some hostname
     * @param port     some port
     * @return Address object added to the pool
     */
    Address add(String hostname, int port);

    /**
     * Remove address from the pool.
     * It does not produce any errors if given address was already deleted.
     *
     * @param address address that should be removed from the pool.
     */
    void remove(Address address);


    /**
     * Get list of all addresses in the pool.
     *
     * @return all addresses in the pool
     */
    List<Address> getAddresses();

    /**
     * Observable source of address change events.
     * Every add/delete operation triggers change event.
     *
     * @return observable address changes
     */
    Observable<AddressChange> getAddressChanges();

}
