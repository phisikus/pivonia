package eu.phisikus.pivonia.middleware;

import eu.phisikus.pivonia.api.middleware.Middleware;
import io.vavr.collection.List;
import lombok.extern.log4j.Log4j2;

import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * Cake binds together multiple layers of middleware by aggregating its message handlers.
 * During initialization each layer of middleware is initialized by providing a reference to MiddlewareClient.
 * That reference allows for the layer to generate messages and push them through parent layers out of the system.
 * The MiddlewareClient also provides an aggregated message handler that triggers processing through all layers below.
 * The assumption is that the top layer will be connected to incoming and outgoing network traffic.
 *
 * @param <T> type of message used in the communication
 */
@Log4j2
public class Cake<T> implements AutoCloseable {

    private Class<T> messageType;
    private List<Middleware<T>> middlewares = List.empty();

    /**
     * Creates Cake instance with given message type
     *
     * @param messageType type of message used in the system
     */
    public Cake(Class<T> messageType) {
        this.messageType = messageType;
    }

    /**
     * Add a layer of middleware.
     *
     * @param middleware middleware to be added
     * @return the same instance of cake but with additional middleware
     */
    public Cake addLayer(Middleware<T> middleware) {
        middlewares = middlewares.append(middleware);
        return this;
    }

    /**
     * It initializes each middleware layer, one by one.
     * Exception can be produced if any layer reports it.
     *
     * @return the same, initialized instance of cake
     * @throws MissingMiddlewareException if any of the layers have unmet dependency
     */
    public Cake initialize() throws MissingMiddlewareException {
        IntStream.range(0, middlewares.size())
                .forEach(ithMiddlewareInitializer());
        return this;
    }

    private IntConsumer ithMiddlewareInitializer() {
        return index -> {
            var middleware = middlewares.get(index);
            var middlewareClient = new MiddlewareClientImpl<>(messageType, middlewares, index);
            middleware.initialize(middlewareClient);
        };
    }

    @Override
    public void close() throws Exception {
        for (var middleware : middlewares) {
            try {
                middleware.close();
            } catch (Exception ignored) {
                log.error(ignored);
            }
        }
    }

}
