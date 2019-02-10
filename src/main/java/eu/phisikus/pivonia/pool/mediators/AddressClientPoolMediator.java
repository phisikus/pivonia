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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Connects address pool and client pool.
 * For each address added to the pool the mediator will create a new connection and add the client to the client pool.
 * Removal of address from the pool will trigger associated client to be removed from the pool and closed.
 */
@Log4j2
class AddressClientPoolMediator implements Disposable {
    private RetryConfig retryConfiguration;
    private Disposable subscription;
    private List<Address> processingAddresses = Collections.synchronizedList(new LinkedList<>());
    private Map<Address, Client> clients = new ConcurrentHashMap<>();

    /**
     * Creates mediator that will add connected client to given Client Pool for each new Address.
     * Client creation and destruction will be triggered by binding with Address Pool event stream.
     * Provided client provider will be used to generate clients before connecting them.
     * Exponential backoff algorithm will be used for retrying connection if it fails with defined maximum retry limit.
     * Each address removal will cause client to be removed from the Client Pool and closed.
     *
     * @param clientPool       client pool where new clients will be added
     * @param addressPool      source of address addition events
     * @param clientProvider   provider of new client instances
     * @param maxRetryAttempts number of connection retry attempts
     */
    public AddressClientPoolMediator(ClientPool clientPool,
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
        handleAddressRemoval(clientPool, addressEvent, address);
        handleAddressAddition(clientPool, clientProvider, addressEvent, address);
    }

    private void handleAddressRemoval(ClientPool clientPool, AddressEvent addressEvent, Address address) {
        if (addressEvent.getOperation() == AddressEvent.Operation.REMOVE) {
            processingAddresses.remove(address);
            var removedClient = clients.remove(address);
            clientPool.remove(removedClient);
            closeClient(removedClient);
        }
    }

    private void handleAddressAddition(ClientPool clientPool, Provider<Client> clientProvider, AddressEvent addressEvent, Address address) {
        if (addressEvent.getOperation() == AddressEvent.Operation.ADD) {
            processingAddresses.add(address);
            connectWithRetry(clientPool, clientProvider, address);
            processingAddresses.remove(address);
        }
    }

    private void connectWithRetry(ClientPool clientPool, Provider<Client> clientProvider, Address address) {
        String retryId = UUID.randomUUID().toString();
        Retry.of(retryId, retryConfiguration)
                .executeSupplier(createClient(clientProvider, address))
                .forEach(client -> {
                    clientPool.add(client);
                    clients.put(address, client);
                });
    }

    private Supplier<Try<Client>> createClient(Provider<Client> clientProvider, Address address) {
        return () -> {
            if (processingAddresses.contains(address)) {
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
