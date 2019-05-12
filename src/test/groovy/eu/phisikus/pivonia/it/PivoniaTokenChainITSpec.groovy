package eu.phisikus.pivonia.it

import eu.phisikus.pivonia.logic.MessageHandler
import eu.phisikus.pivonia.logic.MessageHandlers
import eu.phisikus.pivonia.test.ServerTestUtils
import eu.phisikus.pivonia.utils.Pivonia
import org.awaitility.Duration
import spock.lang.Specification

import java.util.function.BiConsumer

import static org.awaitility.Awaitility.await

class PivoniaTokenChainITSpec extends Specification {

    static final ADDRESS = "localhost"

    def "Should perform mutual exclusion using token correctly"() {
        given: "there are multiple nodes listening on different ports"
        def count = 10
        Map<Integer, Pivonia<Integer, NodeState>> nodes = buildNodes(count)

        and: "the nodes have their right-hand neighbour address set up"
        def ports = nodes.keySet().toList()
        addAddressesToNodes(ports, nodes)

        when: "sending message to the first node"
        def firstNode = nodes.values().first()
        def firstNodePort = ports.first()
        sendFirstMessage(firstNode, firstNodePort)

        then: "token is passed through all of the nodes"
        await().atMost(Duration.TEN_SECONDS).until {
            nodes.inject(true) { result, node ->
                result && node.getValue().getState().isMessageReceived
            }
        }

        cleanup:
        nodes.each { it.getValue().getConnectionManager().dispose() }
    }

    private void sendFirstMessage(Pivonia firstNode, int firstNodePort) {
        def firstMessage = new TokenMessage(0, firstNode.getNodeId(), 0)
        def client = firstNode.getClient()
                .connect(ADDRESS, firstNodePort)
                .get()
        client.send(firstMessage)
        client.close()
    }

    private def addAddressesToNodes(List<Integer> ports, nodes) {
        ports.eachWithIndex { Integer port, int id ->
            if (id < ports.size() - 1) {
                def nextPort = ports.get(id + 1)
                nodes.get(port)
                        .getConnectionManager()
                        .getAddressPool()
                        .add("localhost", nextPort)
            }
        }
    }

    private def buildNodes(int count) {
        def nodes = [:]
        (0..count).forEach({
            def port = ServerTestUtils.getRandomPort()
            nodes.put(port, buildNode(it, port))
        })
        nodes
    }

    private Pivonia<Integer, NodeState> buildNode(int nodeId, int port) {
        BiConsumer<Pivonia<Integer, NodeState>, TokenMessage> tokenHandler = {
            context, message ->
                def state = context.getState()
                def transmitters = context.getConnectionManager().getTransmitterPool()
                def newMessage = new TokenMessage(nodeId, nodeId + 1, state.clock)
                state.clock = message.clock + 1
                state.isMessageReceived = true
                await().until({ transmitters.get(nodeId + 1).isPresent() })
                transmitters.get(nodeId + 1).get().send(newMessage)
        }

        def pivonia = Pivonia.<Integer, NodeState> builder()
                .nodeId(nodeId)
                .state(new NodeState())
                .messageHandlers(MessageHandlers
                        .create()
                        .withHandler(MessageHandler.create(TokenMessage, tokenHandler))
                )
                .build()

        connectServer(pivonia, port)
        return pivonia
    }

    private connectServer(Pivonia<Integer, NodeState> pivonia, int port) {
        pivonia.getConnectionManager()
                .getServerPool()
                .add(pivonia
                        .getServer()
                        .bind(port)
                        .get()
                )
    }

    private class NodeState {
        Integer clock = 0
        boolean isMessageReceived
    }

}
