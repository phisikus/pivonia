package eu.phisikus.pivonia.middleware;

import eu.phisikus.pivonia.api.MessageProcessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Cake<T> {

    private StateContainer stateContainer;
    private List<Middleware<T>> middlewares = new LinkedList<>();

    public Cake(StateContainer stateContainer) {
        this.stateContainer = stateContainer;
        stateContainer.set(Cake.class, this);
    }

    public Cake addLayer(Middleware<T> middleware) {
        middlewares.add(middleware);
        return this;
    }

    public Cake initialize() {
        middlewares.forEach(middleware -> middleware.initialize(stateContainer));
        return this;
    }

    public MessageProcessor<T> getClientSideMessageProcessor() {
        List<MessageProcessor<T>> processors = getProcessorsFromMiddleware(Middleware::getClientSideMessageProcessor);
        return buildAggregatedProcessor(processors);
    }


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
