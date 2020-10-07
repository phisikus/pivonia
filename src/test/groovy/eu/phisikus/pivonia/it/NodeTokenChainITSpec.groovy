package eu.phisikus.pivonia.it

import eu.phisikus.pivonia.logic.MessageHandler
import eu.phisikus.pivonia.logic.MessageHandlers
import eu.phisikus.pivonia.node.Node
import eu.phisikus.pivonia.tcp.utils.AvailablePortProvider
import eu.phisikus.pivonia.test.ServerTestUtils
import spock.lang.Specification

import java.time.Duration
import java.util.function.BiConsumer

import static org.awaitility.Awaitility.await

class NodeTokenChainITSpec extends Specification {

    static final ADDRESS = "localhost"

    def "Should perform mutual exclusion using token correctly"() {
        given: "there are multiple nodes listening on different ports"
        def count = 10
        Map<Integer, Node<Integer, NodeState>> nodes = buildNodes(count)

        and: "the nodes have their right-hand neighbour address set up"
        def ports = nodes.keySet().toList()
        addAddressesToNodes(ports, nodes)

        when: "sending message to the first node"
        def firstNode = nodes.values().first()
        def firstNodePort = ports.first()
        sendFirstMessage(firstNode, firstNodePort)

        then: "token is passed through all of the nodes"
        await().atMost(Duration.ofSeconds(10L)).until {
            nodes.inject(true) {
                result, node -> result && node.getValue().getState().isMessageReceived
            }
        }

        cleanup: "free up resources"
        nodes.each { it.getValue().dispose() }
    }

    private void sendFirstMessage(Node firstNode, int firstNodePort) {
        def firstMessage = new TokenMessage(0, firstNode.getId(), 0)
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
                        .add(ADDRESS, nextPort)
            }
        }
    }

    private def buildNodes(int count) {
        def nodes = [:]
        (0..count).forEach({
            def port = AvailablePortProvider.getRandomPort().get()
            nodes.put(port, buildNode(it, port))
        })
        nodes
    }

    private Node<Integer, NodeState> buildNode(int nodeId, int port) {
        BiConsumer<Node<Integer, NodeState>, TokenMessage> tokenHandler = {
            context, message ->
                def state = context.getState()
                def transmitters = context.getConnectionManager().getTransmitterPool()
                def newMessage = new TokenMessage(nodeId, nodeId + 1, state.clock)
                state.clock = message.clock + 1
                state.isMessageReceived = true
                await().until({ transmitters.get(nodeId + 1).isPresent() })
                transmitters.get(nodeId + 1).get().send(newMessage)
        }

        def node = Node.<Integer, NodeState> builder()
                .id(nodeId)
                .state(new NodeState())
                .messageHandlers(MessageHandlers
                        .create()
                        .withHandler(MessageHandler.create(TokenMessage, tokenHandler))
                )
                .build()

        connectServer(node, port)
        return node
    }

    private connectServer(Node<Integer, NodeState> node, int port) {
        node.getConnectionManager()
                .getServerPool()
                .add(node
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
