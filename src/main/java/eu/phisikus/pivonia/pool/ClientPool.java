package eu.phisikus.pivonia.pool;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import lombok.Value;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

class ClientPool {
    private MessageHandler realMessageHandler;
    private List<ClientPool> entries = new LinkedList<>();

    @Value
    class ClientPoolEntry {
        private Long lastMessageTime;
        private Client client;
        private Function<MessageHandler, Client> clientProvider;
    }

}
