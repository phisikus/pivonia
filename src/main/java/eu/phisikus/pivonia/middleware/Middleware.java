package eu.phisikus.pivonia.middleware;

import eu.phisikus.pivonia.api.MessageProcessor;

public interface Middleware<T> {
    void initialize(StateContainer stateContainer);

    MessageProcessor<T> getClientSideMessageProcessor();

    MessageProcessor<T> getServerSideMessageProcessor();
}
