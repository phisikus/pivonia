package eu.phisikus.pivonia.pool.mediators;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.AddressPool;
import eu.phisikus.pivonia.pool.ClientPool;
import eu.phisikus.pivonia.pool.address.Address;
import eu.phisikus.pivonia.pool.address.AddressEvent;
import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.reactivex.disposables.Disposable;
import io.vavr.control.Try;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Connects address pool and client pool.
 * For each address added to the pool the manager will create a new connection and add the client to the client pool.
 */
public class ClientGenerator implements Disposable {
    private static String RETRY_ID = "client-generation-retry";
    private RetryConfig retryConfiguration;
    private Disposable subscription;

    @Inject
    public ClientGenerator(ClientPool clientPool, AddressPool addressPool, Provider<Client> clientProvider) {
        retryConfiguration = RetryConfig.<Try>custom()
                .maxAttempts(10)
                .intervalFunction(IntervalFunction.ofExponentialBackoff())
                .retryOnResult(Try::isFailure)
                .build();

        bind(clientPool, addressPool, clientProvider);
    }

    private void bind(ClientPool clientPool, AddressPool addressPool, Provider<Client> clientProvider) {
        subscription = addressPool
                .getAddressChanges()
                .filter(addressEvent -> addressEvent.getOperation() == AddressEvent.Operation.ADD)
                .subscribe(addressEvent -> handleAddressAddEvent(clientPool, clientProvider, addressEvent.getAddress()));
    }

    private void handleAddressAddEvent(ClientPool clientPool, Provider<Client> clientProvider, Address address) {
        Retry.of(RETRY_ID, retryConfiguration)
                .executeSupplier(() ->
                        clientProvider.get().connect(address.getHostname(), address.getPort())
                ).forEach(clientPool::add);
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
