package eu.phisikus.pivonia.pool.client;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.ClientPool;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientPoolImpl<K> implements ClientPool<K> {

    @Getter
    private final List<Client> clients = new CopyOnWriteArrayList<>();
    private final Map<K, Client> mappings = new ConcurrentHashMap<>();
    private final Subject<ClientEvent> clientChanges = PublishSubject.create();

    @Override
    public Optional<Client> get(K id) {
        return Optional.ofNullable(mappings.get(id));
    }

    @Override
    public void set(K id, Client client) {
        var previousClient = mappings.put(id, client);
        var isNewValue = previousClient != client;
        var wasPreviouslyAssigned = previousClient != null && isNewValue;

        if (wasPreviouslyAssigned) {
            notify(id, previousClient, ClientEvent.Operation.UNASSIGN);
        }

        if (isNewValue) {
            notify(id, client, ClientEvent.Operation.ASSIGN);
        }
    }

    @Override
    public void add(Client client) {
        clients.add(client);
        notify(null, client, ClientEvent.Operation.ADD);
    }

    @Override
    public void remove(Client client) {
        if (clients.remove(client)) {
            removeMappings(client);
            notify(null, client, ClientEvent.Operation.REMOVE);
        }
    }

    private void removeMappings(Client client) {
        mappings.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(client))
                .forEach(entry -> {
                    mappings.remove(entry.getKey(), entry.getValue());
                    notify(entry.getKey(), entry.getValue(), ClientEvent.Operation.UNASSIGN);
                });
    }

    private void notify(K id, Client client, ClientEvent.Operation assign) {
        var changeEvent = new ClientEvent<>(client, id, assign);
        clientChanges.onNext(changeEvent);
    }

    @Override
    public Observable<ClientEvent> getClientChanges() {
        return clientChanges;
    }
}
