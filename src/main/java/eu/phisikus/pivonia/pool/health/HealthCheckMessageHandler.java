package eu.phisikus.pivonia.pool.health;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

@Builder
class HealthCheckMessageHandler<K, T> implements MessageHandler<T> {

    private K nodeId;
    private final ClientHealthEntry healthEntry;
    private Map<K, Client> clientForNode;
    private EchoMessageFactory<K, T> messageFactory;
    private MessageHandler<T> targetMessageHandler;

    @Override
    public void handleMessage(T incomingMessage, Client client) {
        messageFactory
                .verifyMessage(incomingMessage)
                .ifPresentOrElse(
                        handleEchoSuccess(client),
                        handleOtherMessages(incomingMessage, client));
    }

    private Runnable handleOtherMessages(T incomingMessage, Client client) {
        return () -> targetMessageHandler.handleMessage(incomingMessage, client);
    }

    private Consumer<K> handleEchoSuccess(Client client) {
        return senderId -> {
            synchronized (healthEntry) {
                long currentTime = Instant.now().toEpochMilli();
                clientForNode.put(senderId, client);
                healthEntry.setCurrentClient(client);
                healthEntry.setLastTimeSeen(currentTime);
            }
        };
    }

    @Override
    public Class<T> getMessageType() {
        return targetMessageHandler.getMessageType();
    }
}
