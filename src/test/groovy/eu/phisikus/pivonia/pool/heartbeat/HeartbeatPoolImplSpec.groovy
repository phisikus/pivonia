package eu.phisikus.pivonia.pool.heartbeat

import eu.phisikus.pivonia.api.Client
import spock.lang.Ignore
import spock.lang.Specification

class HeartbeatPoolImplSpec extends Specification {

    @Ignore
    def "Should send heartbeat message after adding client to the pool"() {
        given: "there is a client"
        def client = Mock(Client)

        and: "empty heartbeat pool"
        final nodeId = UUID.randomUUID()
        def pool = new HeartbeatPoolImpl(100L, 1000L, nodeId)

        when: "adding client to the heartbeat pool"
        pool.add(client)

        then: "heartbeat message is sent through the client"
        1 * client.send(_)

    }
}
