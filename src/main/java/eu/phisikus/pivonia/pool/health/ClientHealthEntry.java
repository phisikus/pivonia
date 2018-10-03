package eu.phisikus.pivonia.pool.health;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import lombok.Builder;
import lombok.Data;

import java.util.function.Function;

/**
 * It contains information needed to monitor Client's health.
 *
 * @param <T> type of the message used by client's handler
 */

@Builder
@Data
public class ClientHealthEntry<T> {
    private Long lastTimeSeen;
    private Long reconnectAttempts;
    private Client currentClient;
    private Function<MessageHandler<T>, Client> clientBuilder;
}
