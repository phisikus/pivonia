package eu.phisikus.pivonia.pool.mediators

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.pool.AddressPool
import eu.phisikus.pivonia.pool.ClientPool
import eu.phisikus.pivonia.pool.address.Address
import eu.phisikus.pivonia.pool.address.AddressEvent
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.vavr.control.Try
import spock.lang.Specification

import javax.inject.Provider

class ClientGeneratorSpec extends Specification {

    def "Should successfully generate client"() {
        given: "Client Pool and Address Pool are provided"
        def clientPool = Mock(ClientPool)
        def addressPool = Mock(AddressPool)

        and: "Client provider is prepared"
        def client = Mock(Client)
        def provider = Mock(Provider)

        and: "Address Pool is configured to publish change events"
        def addressChanges = PublishSubject.create()
        addressPool.getAddressChanges() >> addressChanges

        when: "Client Generator is created"
        def generator = new ClientGenerator(clientPool, addressPool, provider, 3)

        and: "new Address is added to the Address Pool"
        def newAddress = new Address("localhost", 7070)
        addressChanges.onNext(new AddressEvent(AddressEvent.Operation.ADD, newAddress))

        then: "Client should be generated"
        1 * provider.get() >> client

        and: "connected using provided address"
        1 * client.connect('localhost', 7070) >> Try.success(client)

        and: "connected client should be added to the Client Pool in a finite time interval"
        1 * clientPool.add(client)

        cleanup: "client generator is destroyed"
        generator.dispose()
    }


    def "Should retry to create and connect client after failure"() {
        given: "Client Pool and Address Pool are provided"
        def clientPool = Mock(ClientPool)
        def addressPool = Mock(AddressPool)

        and: "Client provider is prepared"
        def client = Mock(Client)
        def provider = Mock(Provider)

        and: "Address Pool is configured to publish change events"
        def addressChanges = PublishSubject.create()
        addressPool.getAddressChanges() >> addressChanges

        when: "Client Generator is created"
        def generator = new ClientGenerator(clientPool, addressPool, provider, 3)

        and: "new Address is added to the Address Pool"
        def newAddress = new Address("localhost", 7070)
        addressChanges.onNext(new AddressEvent(AddressEvent.Operation.ADD, newAddress))

        then: "Client provider should be called three times"
        3 * provider.get() >> client

        and: "there should be three connection attempts with only last one successful"
        def failureResult = Try.failure(new IOException())
        3 * client.connect('localhost', 7070) >>> [failureResult, failureResult, Try.success(client)]

        and: "connected client should be added to the Client Pool in a finite time interval"
        1 * clientPool.add(client)

        cleanup: "client generator is destroyed"
        generator.dispose()
    }

    def "Should not add client if the connection never succeeded"() {
        given: "Client Pool and Address Pool are provided"
        def clientPool = Mock(ClientPool)
        def addressPool = Mock(AddressPool)

        and: "Client provider is prepared"
        def client = Mock(Client)
        def provider = Mock(Provider)

        and: "Address Pool is configured to publish change events"
        def addressChanges = PublishSubject.create()
        addressPool.getAddressChanges() >> addressChanges

        when: "Client Generator is created"
        def generator = new ClientGenerator(clientPool, addressPool, provider, 3)

        and: "new Address is added to the Address Pool"
        def newAddress = new Address("localhost", 7070)
        addressChanges.onNext(new AddressEvent(AddressEvent.Operation.ADD, newAddress))

        then: "Client provider should be called multiple times"
        3 * provider.get() >> client

        and: "there should be multiple unsuccessful connection attempts"
        def failureResult = Try.failure(new IOException())
        3 * client.connect('localhost', 7070) >> failureResult

        and: "client should not be added to the Client Pool"
        0 * clientPool.add(client)

        cleanup: "client generator is destroyed"
        generator.dispose()
    }


    def "Should give up on connecting client if address was removed from the address pool"() {
        given: "Client Pool and Address Pool are provided"
        def clientPool = Mock(ClientPool)
        def addressPool = Mock(AddressPool)

        and: "Client provider is prepared"
        def client = Mock(Client)
        def provider = Mock(Provider)

        and: "Address Pool is configured to publish change events in parallel"
        def addressChanges = PublishSubject.create()
        addressPool.getAddressChanges() >> addressChanges.observeOn(Schedulers.io())

        when: "Client Generator is created"
        def generator = new ClientGenerator(clientPool, addressPool, provider, 3)

        and: "new Address is added to the Address Pool"
        def newAddress = new Address("localhost", 7070)
        addressChanges.onNext(new AddressEvent(AddressEvent.Operation.ADD, newAddress))

        and: "Address is removed from the Address Pool"
        addressChanges.onNext(new AddressEvent(AddressEvent.Operation.REMOVE, newAddress))

        then: "Client provider could be called multiple times"
        _ * provider.get() >> client

        and: "there could be an unsuccessful connection attempt before successful one"
        def failureResult = Try.failure(new IOException())
        _ * client.connect("localhost", 7070) >>> [failureResult, failureResult, Try.success(client)]

        and: "client will not be added because address was removed and it stopped connection retry process"
        0 * clientPool.add(client)

        cleanup: "client generator is destroyed"
        generator.dispose()
    }

    def "Should successfully remove generated client on address removal"() {
        given: "Client Pool and Address Pool are provided"
        def clientPool = Mock(ClientPool)
        def addressPool = Mock(AddressPool)

        and: "Client provider is prepared"
        def client = Mock(Client)
        def provider = Mock(Provider)

        and: "Address Pool is configured to publish change events"
        def addressChanges = PublishSubject.create()
        addressPool.getAddressChanges() >> addressChanges

        when: "Client Generator is created"
        def generator = new ClientGenerator(clientPool, addressPool, provider, 3)

        and: "Address is added to the Address Pool"
        1 * provider.get() >> client
        1 * client.connect("localhost", 7070) >> Try.success(client)
        def newAddress = new Address("localhost", 7070)
        addressChanges.onNext(new AddressEvent(AddressEvent.Operation.ADD, newAddress))

        and: "Address is removed from the Address Pool"
        addressChanges.onNext(new AddressEvent(AddressEvent.Operation.REMOVE, newAddress))

        then: "connected client should be removed from the Client Pool"
        1 * clientPool.remove(client)

        and: "client was closed"
        1 * client.close()

        cleanup: "client generator is destroyed"
        generator.dispose()
    }
}
