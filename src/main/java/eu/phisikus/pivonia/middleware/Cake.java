package eu.phisikus.pivonia.middleware;

import eu.phisikus.pivonia.api.middleware.MessageProcessor;
import eu.phisikus.pivonia.api.middleware.Middleware;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cake binds together multiple layers of middleware by aggregating its Message Processors.
 * During initialization each layer of middleware is initialized with a shared StateContainer.
 * In the beginning the only instance available in the state container is Cake itself.
 * To make the Cake functional, one of the used layers should deal with networking.
 * In that layer you can get the Cake instance from state container and get aggregated Message Processors.
 * At that moment your Message Processor can be used as message handler in the initialized client/server.
 *
 * @param <T> type of message used in the communication
 */
public class Cake<T> {

    private StateContainer stateContainer;
    private List<Middleware<T>> middlewares = new LinkedList<>();

    public Cake(StateContainer stateContainer) {
        this.stateContainer = stateContainer;
        stateContainer.set(Cake.class, this);
    }

    /**
     * Add a layer of middleware.
     *
     * @param middleware middleware to be added
     * @return the same instance of cake but with additional middleware
     */
    public Cake addLayer(Middleware<T> middleware) {
        middlewares.add(middleware);
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
        middlewares.forEach(middleware -> middleware.initialize(stateContainer));
        return this;
    }

    /**
     * Get aggregated client-side MessageProcessor.
     * It should call processors from all layers in chain unless one of them returns an empty message.
     *
     * @return MessageProcessor that calls client-side processors from all middleware layers.
     */
    public MessageProcessor<T> getClientSideMessageProcessor() {
        List<MessageProcessor<T>> processors = getProcessorsFromMiddleware(Middleware::getClientSideMessageProcessor);
        return buildAggregatedProcessor(processors);
    }


    /**
     * Get aggregated server-side MessageProcessor.
     * It should call processors from all layers in chain unless one of them returns an empty message.
     *
     * @return MessageProcessor that calls client-side processors from all middleware layers.
     */
    public MessageProcessor<T> getServerSideMessageProcessor() {
        List<MessageProcessor<T>> processors = getProcessorsFromMiddleware(Middleware::getServerSideMessageProcessor);
        return buildAggregatedProcessor(processors);
    }

    private List<MessageProcessor<T>> getProcessorsFromMiddleware(Function<Middleware<T>, MessageProcessor<T>> extractor) {
        return middlewares.stream()
                .map(extractor)
                .collect(Collectors.toList());
    }

    private MessageProcessor<T> buildAggregatedProcessor(List<MessageProcessor<T>> processors) {
        return message -> {
            var currentMessage = Optional.of(message);
            for (MessageProcessor<T> processor : processors) {
                currentMessage = processor.processMessage(currentMessage.get());
                if (!currentMessage.isPresent()) {
                    break;
                }
            }
            return currentMessage;

        };
    }

}
