package eu.phisikus.pivonia.middleware.layer.pool


import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.api.Server
import eu.phisikus.pivonia.api.pool.HasSenderId
import spock.lang.Specification
import spock.lang.Subject

class BasicClientPoolSpec extends Specification {
    final messageHandler = Mock(MessageHandler)
    final randomId = UUID.randomUUID()

    @Subject
    def pool = new BasicClientPool(messageHandler, messageHandler)

    def "Should not return client non-existing node"() {
        expect: "Empty result for random node ID"
        !pool.exists(UUID.randomUUID())
        Optional.empty() == pool.get(UUID.randomUUID())
    }

    def "Should return existing client for given ID"() {
        given: "Client was added to the pool"
        def client = new LoopbackClient()
        pool.addUsingBuilder({ it -> client.connect(null, 0, it) })

        when: "Client receives a message with sender's ID"
        def message = buildMessage(randomId)
        client.send(message)

        then: "It can be found in the client pool"
        pool.get(message.getSenderId()) == Optional.of(client)
    }

    def "Should add client through server"() {
        given: "Client was added to the pool"
        def client = new LoopbackClient()
        pool.addSourceUsingBuilder({ it ->
            client.connect(null, 0, it)
            Mock(Server)
        })

        when: "Client receives a message with sender's ID"
        def message = buildMessage(randomId)
        client.send(message)

        then: "It can be found in the client pool"
        pool.get(message.getSenderId()) == Optional.of(client)

    }

    def "Should check if client exists for given key"() {
        given: "Client was added to the pool"
        def client = new LoopbackClient()
        pool.addUsingBuilder({ it -> client.connect(null, 0, it) })

        when: "Client receives a message"
        def message = buildMessage(randomId)
        client.send(message)

        then: "It exists in the client pool"
        pool.exists(randomId)

    }

    def "Should close clients belonging to the pool"() {
        given: "Clients were added to the pool"
        def firstClient = new LoopbackClient()
        def secondClient = new LoopbackClient()
        pool.addUsingBuilder({ it -> firstClient.connect(null, 0, it) })
        pool.addUsingBuilder({ it -> secondClient.connect(null, 0, it) })

        and: "Clients receive messages"
        def message = buildMessage(UUID.randomUUID())
        def secondMessage = buildMessage(UUID.randomUUID())
        firstClient.send(message)
        secondClient.send(secondMessage)

        when: "Closing the client pool"
        pool.close()

        then: "All of the clients are closed"
        firstClient.isClosed && secondClient.isClosed
    }

    private HasSenderId buildMessage(UUID randomId) {
        new HasSenderId() {
            @Override
            UUID getSenderId() {
                return randomId
            }
        }
    }

}
