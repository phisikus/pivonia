package eu.phisikus.pivonia.it.mutualexclusion;

import eu.phisikus.pivonia.api.EmptyEnvelope;
import eu.phisikus.pivonia.logic.MessageHandler;
import eu.phisikus.pivonia.logic.MessageHandlers;
import eu.phisikus.pivonia.node.Node;
import eu.phisikus.pivonia.test.ServerTestUtils;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.reactivex.disposables.Disposable;
import io.vavr.control.Try;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class RicartAgrawalaNode implements Disposable {
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
    private final ExecutorService senderThread = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isSuccess = new AtomicBoolean(false);

    public RicartAgrawalaNode(Integer nodeId, List<Integer> allNodeIds) {
        this.nodeId = nodeId;
        this.otherNodeIds = allNodeIds
                .stream()
                .filter(otherNodeId -> !otherNodeId.equals(nodeId))
                .collect(Collectors.toList());
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
                .build();

        startServer();
    }

    private void startServer() {
        log.info("Node is starting server (nodeId={}, port={})", nodeId, serverPort);
        var server = node.getServer().bind(serverPort).get();
        node.getConnectionManager().getServerPool().add(server);
    }

    public void requestAccess() {
        var currentClockValue = clock.incrementAndGet();
        currentRequest.set(new Request(nodeId, null, currentClockValue));
        otherNodeIds.stream()
                .forEach(id -> sendMessage(new Request(nodeId, id, currentClockValue)));
    }

    public boolean isSuccess() {
        return isSuccess.get();
    }

    private void sendMessage(EmptyEnvelope<Integer> message) {
        var retryId = UUID.randomUUID().toString();
        var transmitterPool = node.getConnectionManager()
                .getTransmitterPool();
        var transmitter = Retry
                .of(retryId, retryConfiguration)
                .executeSupplier(() -> transmitterPool.get(message.getRecipientId()).orElseThrow(RuntimeException::new));

        senderThread.submit(() -> transmitter.send(message)
                .onSuccess(ignored -> log.info("[{}] sendMessage({}) OK!", nodeId, message))
                .onFailure(ignored -> log.error("[{}] sendMessage({}) FAILED!", nodeId, message))
        );

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
                log.info("[{}] Entering criticalSection! (clock={}, waitingRequests={})", state.nodeId, state.clock, state.waitingRequests);
                waitingRequests.forEach(request -> sendMessage(new Accept(nodeId, request.getSenderId(), currentClock)));
                waitingRequests.clear();
                currentRequest.set(null);
                isSuccess.set(true);
            }

        };
    }


    @Override
    public void dispose() {
        senderThread.shutdown();
        node.dispose();
    }

    @Override
    public boolean isDisposed() {
        return node.isDisposed() && senderThread.isShutdown();
    }
}
