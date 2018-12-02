package eu.phisikus.pivonia.middleware.layer.pool

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
        given: "Server was added to the pool"
        def client = new LoopbackClient()
        pool.addSourceUsingBuilder({ it ->
            client.connect(null, 0, it)
            Try.success(Mock(Server))
        })

        when: "Server receives a message with sender's ID"
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

    def "Should associate client with node ID manually"() {
        given: "Client is defined"
        def client = Mock(Client)

        when: "Client is associated with node ID"
        pool.set(randomId, client)

        then: "It can be retrieved for that ID"
        pool.get(randomId) == Optional.of(client)
    }

    def "Should remove association between node ID and Client"() {
        given: "There is association"
        def client = Mock(Client)
        pool.set(randomId, client)

        when: "Association is removed"
        pool.remove(client)

        then: "It cannot be retrieved using ID"
        !pool.exists(randomId)
    }

    def "Should expose client messages as a stream"() {
        given: "There is connected client"
        def client = pool.addUsingBuilder({
            messageHandler -> new LoopbackClient().connect(null, 0, messageHandler)
        }).get()

        and: "Message stream is monitored"
        def testSubscriber = new TestObserver()
        pool.getClientMessages().subscribe(testSubscriber)

        when: "Message is sent to the client"
        def message = buildMessage(randomId)
        client.send(message)

        then: "Message is passed to the message stream"
        testSubscriber.assertSubscribed()
        testSubscriber.assertNoErrors()
        testSubscriber.values() == [new MessageWithClient(message, client)]
    }


    def "Should expose server messages as a stream"() {
        given: "There is connected client"
        def serverMessageHandler = null
        pool.addSourceUsingBuilder({
            messageHandler ->
                serverMessageHandler = messageHandler
                return Try.success(Mock(Server))
        }).get()

        and: "Message stream is monitored"
        def testSubscriber = new TestObserver()
        pool.getServerMessages().subscribe(testSubscriber)

        when: "Message is sent to the client"
        def message = buildMessage(randomId)
        def client = Mock(Client)
        serverMessageHandler.handleMessage(message, client)

        then: "Message is passed to the message stream"
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
        }
    }

}