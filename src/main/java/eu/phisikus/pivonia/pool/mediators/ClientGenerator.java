package eu.phisikus.pivonia.pool.mediators;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.AddressPool;
import eu.phisikus.pivonia.pool.ClientPool;
import eu.phisikus.pivonia.pool.address.Address;
import eu.phisikus.pivonia.pool.address.AddressEvent;
import io.reactivex.disposables.Disposable;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Connects address pool and client pool.
 * For each address added to the pool the manager will create a new connection and add the client to the client pool.
 */
public class ClientGenerator implements Disposable {
    private Disposable subscription;

    // TODO complete, add tests
    @Inject
    public ClientGenerator(ClientPool clientPool, AddressPool addressPool, Provider<Client> clientProvider) {
        bind(clientPool, addressPool, clientProvider);
    }

    private void bind(ClientPool clientPool, AddressPool addressPool, Provider<Client> clientProvider) {
        subscription = addressPool.getAddressChanges()
                .subscribe(addressEvent -> {
                    if (addressEvent.getOperation() == AddressEvent.Operation.ADD) {
                        handleAddressAddEvent(clientPool, clientProvider, addressEvent.getAddress());
                    }
                });
    }

    private void handleAddressAddEvent(ClientPool clientPool, Provider<Client> clientProvider, Address address) {
        var connectedClient = clientProvider.get().connect(address.getHostname(), address.getPort());
        connectedClient.forEach(clientPool::add);
    }

    @Override
    public void dispose() {
        subscription.dispose();
    }

    @Override
    public boolean isDisposed() {
        return subscription.isDisposed();
    }
}
