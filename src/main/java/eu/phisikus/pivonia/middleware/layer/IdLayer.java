package eu.phisikus.pivonia.middleware.layer;

import eu.phisikus.pivonia.api.middleware.Middleware;
import eu.phisikus.pivonia.api.middleware.MiddlewareClient;
import eu.phisikus.pivonia.api.pool.Envelope;
import eu.phisikus.pivonia.middleware.MissingMiddlewareException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

/**
 *
 * This layer allows you to identify all outgoing communication with single sender ID.
 * It also filters out all messages with recipient IDs that do not match defined ID.
 *
 * @param <K> type of key used as ID
 * @param <T> type of message passed through the system
 */
@AllArgsConstructor
public class IdLayer<K, T extends Envelope<K>> implements Middleware<T> {

    @Getter
    private final K id;

    @Override
    public void initialize(MiddlewareClient<T> middlewareClient) throws MissingMiddlewareException {
        // nothing to do here
    }

    @Override
    public Optional<T> handleIncomingMessage(T message) {
        if (id.equals(message.getRecipientId())) {
            return Optional.of(message);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<T> handleOutgoingMessage(T message) {
        T newEnvelope = (T) message.readress(id, message.getRecipientId());
        return Optional.of(newEnvelope);
    }

    @Override
    public void close() throws Exception {
        // nothing to do here
    }
}
