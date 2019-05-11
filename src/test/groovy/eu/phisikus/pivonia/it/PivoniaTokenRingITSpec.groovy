package eu.phisikus.pivonia.it


import eu.phisikus.pivonia.logic.MessageHandler
import eu.phisikus.pivonia.logic.MessageHandlers
import eu.phisikus.pivonia.test.ServerTestUtils
import eu.phisikus.pivonia.utils.Pivonia
import org.apache.logging.log4j.LogManager
import spock.lang.Ignore
import spock.lang.Specification

import java.util.function.BiConsumer

class PivoniaTokenRingITSpec extends Specification {
    @Ignore
    def "Should perform mutual exclusion using token correctly"() {
        given: "there are multiple nodes"
        def firstNode = buildNode()
        def secondNode = buildNode()
        def thirdNode = buildNode()

        and: "they know their right hand neighbour"
        firstNode.getConnectionManager().getAddressPool().add("localhost", secondNode.getNodeId())
        secondNode.getConnectionManager().getAddressPool().add("localhost", thirdNode.getNodeId())
        thirdNode.getConnectionManager().getAddressPool().add("localhost", firstNode.getNodeId())

        when: "sending message to the first node"
        firstNode.getClient()
                .connect("localhost", firstNode.getNodeId())
                .get()
                .send(new TokenMessage(0, firstNode.getNodeId(), 0))

        then: ""
        sleep(40000)

        cleanup:
        firstNode.getConnectionManager().dispose()
        secondNode.getConnectionManager().dispose()
        thirdNode.getConnectionManager().dispose()

    }

    private Pivonia<Integer, NodeState> buildNode() {
        def nodeId = ServerTestUtils.getRandomPort()
        def logger = LogManager.getLogger("node" + nodeId)
        BiConsumer<Pivonia<Integer, NodeState>, TokenMessage> tokenHandler = {
            context, message ->
                synchronized (context) {
                    logger.info("Received message {nodeId={}, message={}}", nodeId, message)
                    def state = context.getState()
                    def pool = context.getConnectionManager().getTransmitterPool()
                    while (pool.getTransmitters().isEmpty()) {
                        sleep(200) // ugly wait for client connection ;)
                    }
                    state.clock = message.clock + 1
                    pool.getTransmitters().forEach({
                        logger.info("Sending message from {}", nodeId)
                        it.send(new TokenMessage(state.clock, nodeId, state.clock))
                    })
                }
        }
        def pivonia = Pivonia.<Integer, NodeState> builder()
                .nodeId(nodeId)
                .state(new NodeState())
                .messageHandlers(MessageHandlers.create()
                        .withHandler(MessageHandler.create(TokenMessage, tokenHandler))
                )
                .build()

        def server = pivonia.getServer()
                .bind(nodeId)
                .get()

        pivonia.getConnectionManager().getServerPool().add(server)
        return pivonia
    }

    private class NodeState {
        Integer clock = 0
    }

}
