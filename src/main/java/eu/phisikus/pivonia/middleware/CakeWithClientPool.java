package eu.phisikus.pivonia.middleware;

import eu.phisikus.pivonia.api.pool.ClientPool;
import eu.phisikus.pivonia.api.pool.Envelope;
import eu.phisikus.pivonia.middleware.layer.pool.ClientPoolImpl;
import eu.phisikus.pivonia.middleware.layer.pool.ClientPoolLayer;
import lombok.Getter;

public class CakeWithClientPool<K, T extends Envelope<K>> extends Cake<T> {
    @Getter
    private final ClientPool<K, T> clientPool;
    private final ClientPoolLayer<K, T> clientPoolLayer;

    public CakeWithClientPool(Class<T> messageType) {
        super(messageType);
        clientPool = new ClientPoolImpl<>(messageType);
        clientPoolLayer = new ClientPoolLayer<>(clientPool);
        addLayer(clientPoolLayer);
    }
}
