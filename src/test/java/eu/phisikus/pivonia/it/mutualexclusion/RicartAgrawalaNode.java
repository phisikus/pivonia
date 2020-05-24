package eu.phisikus.pivonia.it.mutualexclusion;

import eu.phisikus.pivonia.api.EmptyEnvelope;
import eu.phisikus.pivonia.logic.MessageHandler;
import eu.phisikus.pivonia.logic.MessageHandlers;
import eu.phisikus.pivonia.node.Node;
import eu.phisikus.pivonia.test.ServerTestUtils;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RicartAgrawalaNode {
    private static final Logger log = LogManager.getLogger(RicartAgrawalaNode.class);
    private final Integer nodeId;
    private final List<Integer> otherNodeIds;
    private final Integer serverPort;
    private final AtomicInteger clock = new AtomicInteger(0);
    private final AtomicReference<Request> currentRequest;
    private final List<Request> waitingRequests = Collections.synchronizedList(new LinkedList<>());
    private final List<Accept> approvals = Collections.synchronizedList(new LinkedList<>());
    private final Node<Integer, RicartAgrawalaNode> node;
    private final RetryConfig retryConfiguration;
    private final Predicate<Try> ifFailureButNotExitCode = result -> result.isFailure() &&
            !NoSuchElementException.class
                    .equals(result.getCause().getClass());

    public RicartAgrawalaNode(Integer nodeId, List<Integer> allNodeIds) {
        this.nodeId = nodeId;
        this.otherNodeIds = allNodeIds.stream().filter(otherNodeId -> !otherNodeId.equals(nodeId)).collect(Collectors.toList());
        this.serverPort = ServerTestUtils.getRandomPort();
        this.currentRequest = new AtomicReference<>(new Request(nodeId, null, clock.get()));
        this.node = Node.<Integer, RicartAgrawalaNode>builder()
                .id(nodeId)
                .state(this)
                .heartbeatDelay(500L)
                .messageHandlers(
                        MessageHandlers.<Node<Integer, RicartAgrawalaNode>>create()
                                .withHandler(MessageHandler.create(Request.class, onRequest()))
                                .withHandler(MessageHandler.create(Accept.class, onAccept()))
                )
                .build();


        retryConfiguration = RetryConfig.<Try>custom()
                .maxAttempts(10)
                .intervalFunction(IntervalFunction.ofExponentialBackoff())
                .retryOnResult(ifFailureButNotExitCode)
                .build();

        startServer();
    }

    private void startServer() {
        var server = node.getServer().bind(serverPort).get();
        node.getConnectionManager().getServerPool().add(server);
    }

    public void requestAccess() {
        var currentClockValue = clock.incrementAndGet();
        currentRequest.set(new Request(nodeId, null, currentClockValue));
        otherNodeIds.stream()
                .forEach(id -> sendMessage(new Request(nodeId, id, currentClockValue)));
    }

    private void sendMessage(EmptyEnvelope<Integer> message) {
        var retryId = UUID.randomUUID().toString();
        var transmitterPool = node.getConnectionManager()
                .getTransmitterPool();
        Retry.of(retryId, retryConfiguration)
                .executeSupplier(() -> transmitterPool.get(message.getRecipientId()))
                .map(transmitter -> {

                    return transmitter.send(message).getOrElseThrow((Supplier<RuntimeException>) RuntimeException::new);
                })
                .orElseThrow(RuntimeException::new);
    }

    @NonNull
    private BiConsumer<Node<Integer, RicartAgrawalaNode>, Request> onRequest() {
        return (node, request) -> {
            var state = node.getState();
            var currentNodeRequest = state.currentRequest.get();
            if (request.isBetterThan(currentNodeRequest)) {
                var targetNodeId = request.getSenderId();
                var currentClock = state.clock.incrementAndGet();
                var accept = new Accept(state.nodeId, targetNodeId, currentClock);
                sendMessage(accept);
            } else {
                state.waitingRequests.add(request);
            }
        };
    }


    @NonNull
    private BiConsumer<Node<Integer, RicartAgrawalaNode>, Accept> onAccept() {
        return (node, accept) -> {
            var state = node.getState();
            var currentClock = state.clock.incrementAndGet();
            state.approvals.add(accept);
            var isCriticalSectionAvailable = approvals
                    .stream()
                    .map(Accept::getSenderId)
                    .collect(Collectors.toSet())
                    .containsAll(otherNodeIds);
            if (isCriticalSectionAvailable) {
                log.info("Entering criticalSection! (nodeId={}, clock={}, waitingRequests={})", state.nodeId, state.clock, state.waitingRequests);
                waitingRequests.forEach(request -> {
                    sendMessage(new Request(nodeId, request.getSenderId(), currentClock));
                });
                waitingRequests.clear();
            }

        };
    }


}
