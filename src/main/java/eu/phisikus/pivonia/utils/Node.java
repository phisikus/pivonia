package eu.phisikus.pivonia.utils;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.converter.ConverterComponent;
import eu.phisikus.pivonia.converter.DaggerConverterComponent;
import eu.phisikus.pivonia.crypto.CryptoComponent;
import eu.phisikus.pivonia.crypto.CryptoModule;
import eu.phisikus.pivonia.crypto.DaggerCryptoComponent;
import eu.phisikus.pivonia.logic.LogicProviderDecorator;
import eu.phisikus.pivonia.logic.MessageHandlers;
import eu.phisikus.pivonia.pool.ConnectionManager;
import eu.phisikus.pivonia.pool.DaggerPoolComponent;
import eu.phisikus.pivonia.pool.PoolComponent;
import eu.phisikus.pivonia.pool.PoolModule;
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatPoolModule;
import eu.phisikus.pivonia.tcp.DaggerTCPComponent;
import eu.phisikus.pivonia.tcp.TCPComponent;
import io.vavr.Lazy;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import javax.inject.Provider;
import java.util.List;

/**
 * Node represents configured facade for this framework.
 * It provides standalone instances of encrypted and cleartext TCP Clients and Servers.
 * It also provides more complex ConnectionManager which contains connection pools with heartbeat protocol capability.
 *
 * @param <K> type of node ID
 * @param <S> type of state object
 */
public class Node<K, S> implements TCPComponent {

    @Getter
    private K id;
    @Getter
    private S state;
    private MessageHandlers messageHandlers;
    private long heartbeatDelay;
    private long timeoutDelay;
    private int maxConnectionRetryAttempts;
    private byte[] encryptionKey;
    private Lazy<TCPComponent> tcpComponent;
    private Lazy<ConnectionManager> connectionManager;
    private List<Middleware<K, S>> middlewares;

    @Builder
    Node(@NonNull K id,
         @NonNull MessageHandlers messageHandlers,
         @Singular("middleware") List<Middleware<K, S>> middlewares,
         S state,
         Long heartbeatDelay,
         Long timeoutDelay,
         Integer maxConnectionRetryAttempts,
         byte[] encryptionKey) {
        this.id = id;
        this.state = state;
        this.middlewares = middlewares;
        this.messageHandlers = buildHandlers(messageHandlers, middlewares);
        this.heartbeatDelay = heartbeatDelay == null ? 5000 : heartbeatDelay;
        this.timeoutDelay = timeoutDelay == null ? 20000 : timeoutDelay;
        this.maxConnectionRetryAttempts = maxConnectionRetryAttempts == null ? 10 : maxConnectionRetryAttempts;
        this.encryptionKey = encryptionKey;
        this.tcpComponent = Lazy.of(() -> {
            CryptoComponent cryptoComponent = getCryptoComponent();
            ConverterComponent converterComponent = getConverterComponent(cryptoComponent);
            return getTcpComponent(converterComponent);
        });

        this.connectionManager = Lazy.of(() -> {
            Provider<Client> clientProvider = getClientProvider(tcpComponent.get());
            return getPoolComponent(clientProvider).getConnectionManager();
        });
        middlewares.forEach(middleware -> middleware.init(this));
    }

    private MessageHandlers buildHandlers(MessageHandlers<Node<K,S>> messageHandlers, List<Middleware<K, S>> middlewares) {
        return middlewares.stream()
                .map(Middleware::getMessageHandlers)
                .reduce(messageHandlers, MessageHandlers::withHandlers)
                .build(this);
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager.get();
    }


    private PoolComponent getPoolComponent(Provider<Client> clientProvider) {
        return DaggerPoolComponent.builder()
                .poolModule(new PoolModule(clientProvider, maxConnectionRetryAttempts))
                .heartbeatPoolModule(new HeartbeatPoolModule(heartbeatDelay, timeoutDelay, id))
                .build();
    }

    private Provider<Client> getClientProvider(TCPComponent tcpComponent) {
        Provider<Client> baseClientProvider = () ->
                encryptionKey == null ? tcpComponent.getClient() : tcpComponent.getClientWithEncryption();
        return new LogicProviderDecorator<>(baseClientProvider, messageHandlers);
    }

    private TCPComponent getTcpComponent(ConverterComponent converterComponent) {
        return DaggerTCPComponent.builder()
                .converterComponent(converterComponent)
                .build();
    }

    private ConverterComponent getConverterComponent(CryptoComponent cryptoComponent) {
        return DaggerConverterComponent.builder()
                .cryptoComponent(cryptoComponent)
                .build();
    }

    private CryptoComponent getCryptoComponent() {
        return DaggerCryptoComponent.builder()
                .cryptoModule(new CryptoModule(encryptionKey))
                .build();
    }

    @Override
    public Client getClient() {
        Provider<Client> clientProvider = () -> tcpComponent.get().getClient();
        return new LogicProviderDecorator<>(clientProvider, messageHandlers).get();
    }

    @Override
    public Server getServer() {
        Provider<Server> serverProvider = () -> tcpComponent.get().getServer();
        return new LogicProviderDecorator<>(serverProvider, messageHandlers).get();
    }

    @Override
    public Client getClientWithEncryption() {
        Provider<Client> encryptedClientProvider = () -> tcpComponent.get().getClientWithEncryption();
        return new LogicProviderDecorator<>(encryptedClientProvider, messageHandlers).get();
    }

    @Override
    public Server getServerWithEncryption() {
        Provider<Server> encryptedServerProvider = () -> tcpComponent.get().getServerWithEncryption();
        return new LogicProviderDecorator<>(encryptedServerProvider, messageHandlers).get();
    }
}
