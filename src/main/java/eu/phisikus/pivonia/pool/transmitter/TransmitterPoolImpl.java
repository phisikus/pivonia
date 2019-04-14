package eu.phisikus.pivonia.pool.transmitter;

import eu.phisikus.pivonia.api.Transmitter;
import eu.phisikus.pivonia.pool.TransmitterPool;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class TransmitterPoolImpl<K> implements TransmitterPool<K> {

    @Getter
    private final List<Transmitter> transmitters = new CopyOnWriteArrayList<>();
    private final Map<K, Transmitter> mappings = new ConcurrentHashMap<>();
    private final Subject<TransmitterPoolEvent> poolChanges = PublishSubject.create();

    @Override
    public Optional<Transmitter> get(K id) {
        return Optional.ofNullable(mappings.get(id));
    }

    @Override
    public void set(K id, Transmitter transmitter) {
        var previousTransmitter = mappings.put(id, transmitter);
        var isNewValue = previousTransmitter != transmitter;
        var wasPreviouslyAssigned = previousTransmitter != null && isNewValue;

        if (wasPreviouslyAssigned) {
            notify(id, previousTransmitter, TransmitterPoolEvent.Operation.UNASSIGN);
        }

        if (isNewValue) {
            notify(id, transmitter, TransmitterPoolEvent.Operation.ASSIGN);
        }
    }

    @Override
    public void add(Transmitter transmitter) {
        transmitters.add(transmitter);
        notify(null, transmitter, TransmitterPoolEvent.Operation.ADD);
    }

    @Override
    public void remove(Transmitter transmitter) {
        if (transmitters.remove(transmitter)) {
            removeMappings(transmitter);
            notify(null, transmitter, TransmitterPoolEvent.Operation.REMOVE);
        }
    }

    private void removeMappings(Transmitter transmitter) {
        mappings.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(transmitter))
                .forEach(entry -> {
                    mappings.remove(entry.getKey(), entry.getValue());
                    notify(entry.getKey(), entry.getValue(), TransmitterPoolEvent.Operation.UNASSIGN);
                });
    }

    private void notify(K id, Transmitter transmitter, TransmitterPoolEvent.Operation assign) {
        var changeEvent = new TransmitterPoolEvent<>(transmitter, id, assign);
        poolChanges.onNext(changeEvent);
    }

    @Override
    public Observable<TransmitterPoolEvent> getChanges() {
        return poolChanges;
    }
}
