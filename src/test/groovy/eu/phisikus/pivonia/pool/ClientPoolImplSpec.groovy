package eu.phisikus.pivonia.pool

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.Server
import eu.phisikus.pivonia.api.pool.Envelope
import eu.phisikus.pivonia.api.pool.MessageWithClient
import io.reactivex.observers.TestObserver
import io.vavr.control.Try
import spock.lang.Specification
import spock.lang.Subject

class ClientPoolImplSpec extends Specification {
    final randomId = UUID.randomUUID()

    @Subject
    def pool = new ClientPoolImpl()

    void cleanup() {
        pool.close()
    }

    def "Should not return client for non-existing node"() {
        expect: "empty result for random node ID"
        !pool.exists(UUID.randomUUID())
        Optional.empty() == pool.get(UUID.randomUUID())
    }

    def "Should return existing client for given ID"() {
        given: "client was added to the pool"
        def client = new LoopbackClient()
        pool.addUsingBuilder({ handler -> client.addHandler(handler).connect(null, 0) })

        when: "client receives a message with sender's ID"
        def message = buildMessage(randomId)
        client.send(message)

        then: "it can be found in the client pool"
        pool.get(message.getSenderId()) == Optional.of(client)
    }

    def "Should check if client exists for given ID"() {
        given: "client was added to the pool"
        def client = new LoopbackClient()
        pool.addUsingBuilder({ handler -> client.addHandler(handler).connect(null, 0) })

        when: "client receives a message"
        def message = buildMessage(randomId)
        client.send(message)

        then: "it exists in the client pool"
        pool.exists(randomId)
    }

    def "Should add client when message comes through registered server"() {
        given: "server was added to the pool"
        def client = new LoopbackClient()
        pool.addSourceUsingBuilder({ handler ->
            client.addHandler(handler).connect(null, 0)
            Try.success(Mock(Server))
        })

        when: "server receives a message with sender's ID"
        def message = buildMessage(randomId)
        client.send(message)

        then: "it can be found in the client pool"
        pool.get(message.getSenderId()) == Optional.of(client)
    }

    def "Should close clients belonging to the pool"() {
        given: "clients were added to the pool"
        def firstClient = new LoopbackClient()
        def secondClient = new LoopbackClient()
        pool.addUsingBuilder({ handler -> firstClient.addHandler(handler).connect(null, 0) })
        pool.addUsingBuilder({ handler -> secondClient.addHandler(handler).connect(null, 0) })

        and: "clients received messages"
        def message = buildMessage(UUID.randomUUID())
        def secondMessage = buildMessage(UUID.randomUUID())
        firstClient.send(message)
        secondClient.send(secondMessage)

        when: "closing the client pool"
        pool.close()

        then: "all of the clients are closed"
        firstClient.isClosed && secondClient.isClosed
    }

    def "Should associate client with node ID manually"() {
        given: "client is defined"
        def client = Mock(Client)

        when: "client is associated with node ID"
        pool.set(randomId, client)

        then: "it can be retrieved for that ID"
        pool.get(randomId) == Optional.of(client)
    }

    def "Should remove association between node ID and Client"() {
        given: "there is association"
        def client = Mock(Client)
        pool.set(randomId, client)

        when: "association is removed"
        pool.remove(client)

        then: "it cannot be retrieved using ID"
        !pool.exists(randomId)
    }

    def "Should expose client messages as a stream"() {
        given: "there is connected client"
        def client = pool.addUsingBuilder({
            messageHandler -> new LoopbackClient().addHandler(messageHandler).connect(null, 0)
        }).get()

        and: "message stream is monitored"
        def testSubscriber = new TestObserver()
        pool.getClientMessages().subscribe(testSubscriber)

        when: "message is sent to the client"
        def message = buildMessage(randomId)
        client.send(message)

        then: "message is passed to the message stream"
        testSubscriber.assertSubscribed()
        testSubscriber.assertNoErrors()
        testSubscriber.values() == [new MessageWithClient(message, client)]
    }

    def "Should expose server messages as a stream"() {
        given: "there is connected client"
        def serverMessageHandler = null
        pool.addSourceUsingBuilder({
            messageHandler ->
                serverMessageHandler = messageHandler
                return Try.success(Mock(Server))
        }).get()

        and: "message stream is monitored"
        def testSubscriber = new TestObserver()
        pool.getServerMessages().subscribe(testSubscriber)

        when: "message is sent to the client"
        def message = buildMessage(randomId)
        def client = Mock(Client)
        serverMessageHandler.handleMessage(message, client)

        then: "message is passed to the message stream"
        testSubscriber.assertSubscribed()
        testSubscriber.assertNoErrors()
        testSubscriber.values() == [new MessageWithClient(message, client)]
    }

    private Envelope buildMessage(UUID randomId) {
        new Envelope() {
            @Override
            UUID getSenderId() {
                return randomId
            }

            @Override
            Object getRecipientId() {
                return randomId
            }

            @Override
            Envelope readdress(Object senderId, Object recipientId) {
                return this
            }
        }
    }

}
