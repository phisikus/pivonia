package eu.phisikus.pivonia.pool.mediators

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.pool.AddressPool
import eu.phisikus.pivonia.pool.ClientPool
import eu.phisikus.pivonia.pool.address.Address
import eu.phisikus.pivonia.pool.address.AddressEvent
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

        and: "connected client should be added to the Client Pool in a finite time interval"
        0 * clientPool.add(client)

        cleanup: "client generator is destroyed"
        generator.dispose()
    }
}
