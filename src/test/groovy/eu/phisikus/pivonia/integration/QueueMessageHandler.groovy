package eu.phisikus.pivonia.integration

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageHandler

class QueueMessageHandler implements MessageHandler<TimeMessage> {
    Queue<TimeMessage> messageQueue

    QueueMessageHandler(Queue<TimeMessage> messageQueue) {
        this.messageQueue = messageQueue
    }

    @Override
    void handleMessage(TimeMessage incomingMessage, Client client) {
        messageQueue.add(incomingMessage)
    }

    @Override
    Class<TimeMessage> getMessageType() {
        return TimeMessage
    }
}
