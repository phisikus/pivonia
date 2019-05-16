package eu.phisikus.pivonia.pool.transmitter;

import eu.phisikus.pivonia.api.Transmitter;
import eu.phisikus.pivonia.pool.TransmitterPool;
import eu.phisikus.pivonia.pool.transmitter.events.*;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Log4j2
class TransmitterPoolImpl<K> implements TransmitterPool<K> {

    @Getter
    private final List<Transmitter> transmitters = new CopyOnWriteArrayList<>();
    private final Map<K, Transmitter> mappings = new ConcurrentHashMap<>();
    private final Subject<TransmitterPoolEvent> poolChanges = PublishSubject.create();
    @Getter
    private boolean isDisposed = false;

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
            notify(new UnassignmentEvent<>(id, previousTransmitter));
        }

        if (isNewValue) {
            notify(new AssignmentEvent<>(id, transmitter));
        }
    }

    @Override
    public void add(Transmitter transmitter) {
        transmitters.add(transmitter);
        notify(new AdditionEvent(transmitter));
    }

    @Override
    public void remove(Transmitter transmitter) {
        if (transmitters.remove(transmitter)) {
            removeMappings(transmitter);
            notify(new RemovalEvent(transmitter));
        }
    }

    private void removeMappings(Transmitter transmitter) {
        mappings.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(transmitter))
                .forEach(entry -> {
                    mappings.remove(entry.getKey(), entry.getValue());
                    var changeEvent = new UnassignmentEvent<>(entry.getKey(), entry.getValue());
                    notify(changeEvent);
                });
    }

    private void notify(TransmitterPoolEvent changeEvent) {
        poolChanges.onNext(changeEvent);
    }

    @Override
    public Observable<TransmitterPoolEvent> getChanges() {
        return poolChanges;
    }

    @Override
    public void dispose() {
        if(!isDisposed) {
            transmitters.forEach(transmitter -> {
                try {
                    transmitter.close();
                } catch (Exception exception) {
                    log.debug(exception);
                }
            });
        }
        isDisposed = true;
    }

}
