package eu.phisikus.pivonia.pool.heartbeat

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageWithTransmitter
import eu.phisikus.pivonia.api.Server
import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

import java.time.Instant

class HeartbeatServerVisitorSpec extends Specification {

    def "Should register working heartbeat message handler"() {
        given: "Server instance is prepared"
        def server = Mock(Server)
        def nodeId = UUID.randomUUID()

        and: "message source is configured"
        def messageSource = PublishSubject.create()
        def client = Mock(Client)

        when: "heartbeat algorithm is registered"
        HeartbeatServerVisitor.registerHeartbeatListener(nodeId, server)

        and: "heartbeat message is sent to the server"
        def heartbeatRequest = new HeartbeatMessage<>("001", 0)
        def heartbeat = new MessageWithTransmitter(heartbeatRequest, client)
        messageSource.onNext(heartbeat)

        then: "message listener is registered"
        1 * server.getMessages(HeartbeatMessage) >> messageSource

        and: "response is sent"
        1 * client.send({
            it.senderId == nodeId &&
                    it.timestamp <= Instant.now().toEpochMilli()
        })

    }
}
