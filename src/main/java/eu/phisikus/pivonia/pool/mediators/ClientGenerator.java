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
import lombok.extern.log4j.Log4j2;

import javax.inject.Provider;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Connects address pool and client pool.
 * For each address added to the pool the manager will create a new connection and add the client to the client pool.
 */
@Log4j2
public class ClientGenerator implements Disposable {
    private RetryConfig retryConfiguration;
    private Disposable subscription;
    private List<Address> monitoredAddresses = Collections.synchronizedList(new LinkedList<>());

    public ClientGenerator(ClientPool clientPool,
                           AddressPool addressPool,
                           Provider<Client> clientProvider,
                           int maxRetryAttempts) {
        Predicate<Try> ifFailureButNotExitCode = result -> result.isFailure() &&
                !NoSuchElementException.class
                        .equals(result.getCause().getClass());

        retryConfiguration = RetryConfig.<Try>custom()
                .maxAttempts(maxRetryAttempts)
                .intervalFunction(IntervalFunction.ofExponentialBackoff())
                .retryOnResult(ifFailureButNotExitCode)
                .build();

        bind(clientPool, addressPool, clientProvider);
    }

    private void bind(ClientPool clientPool, AddressPool addressPool, Provider<Client> clientProvider) {
        subscription = addressPool
                .getAddressChanges()
                .subscribe(addressEvent -> handleAddressEvent(clientPool, clientProvider, addressEvent));
    }

    private void handleAddressEvent(ClientPool clientPool, Provider<Client> clientProvider, AddressEvent addressEvent) {
        Address address = addressEvent.getAddress();
        if (addressEvent.getOperation() == AddressEvent.Operation.REMOVE) {
            monitoredAddresses.remove(address);
        }

        if (addressEvent.getOperation() == AddressEvent.Operation.ADD) {
            monitoredAddresses.add(address);
            connectWithRetry(clientPool, clientProvider, address);
        }
    }

    private void connectWithRetry(ClientPool clientPool, Provider<Client> clientProvider, Address address) {
        String retryId = UUID.randomUUID().toString();
        Retry.of(retryId, retryConfiguration)
                .executeSupplier(createClient(clientProvider, address))
                .forEach(clientPool::add);
    }

    private Supplier<Try<Client>> createClient(Provider<Client> clientProvider, Address address) {
        return () -> {
            if (monitoredAddresses.contains(address)) {
                var newClient = clientProvider.get();
                var connectionResult = newClient.connect(address.getHostname(), address.getPort());
                connectionResult.onFailure(throwable -> closeClient(newClient));
                return connectionResult;
            }
            return Try.failure(new NoSuchElementException());
        };
    }

    private void closeClient(Client client) {
        try {
            client.close();
        } catch (Exception e) {
            log.error("Unexpected exception occurred when closing client that was unable to connect", e);
        }
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
