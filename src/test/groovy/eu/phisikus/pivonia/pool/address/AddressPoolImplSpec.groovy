package eu.phisikus.pivonia.pool.address

import io.reactivex.Observer
import spock.lang.Specification

class AddressPoolImplSpec extends Specification {

    def "Should retrieve addresses from pool properly"() {
        given: "empty address pool"
        def pool = new AddressPoolImpl()

        when: "addresses are added to the pool"
        pool.add("host1", 8080)
        pool.add("host2", 8081)
        pool.add("host3", 22)

        then: "those addresses can be retrieved from the pool"
        verifyAll(pool.getAddresses()) {
            contains(new Address("host3", 22))
            contains(new Address("host2", 8081))
            contains(new Address("host1", 8080))
        }
    }

    def "Should delete address from the pool"() {
        given: "there is address pool with one host in it"
        def pool = new AddressPoolImpl()
        def address = pool.add("host4", 4040)

        and: "defined expected deletion event"
        def expectedEvent = new AddressPoolEvent(AddressPoolEvent.Operation.REMOVE, address)

        and: "pool is monitored"
        def listener = Mock(Observer)
        pool.getChanges().subscribe(listener)

        when: "address is removed"
        pool.remove(address)

        then: "the pool does not contain given address"
        !pool.getAddresses().contains(address)

        and: "deletion event was emitted"
        1 * listener.onNext(expectedEvent)
    }

    def "Should add address to the pool"() {
        given: "there is an empty address pool"
        def pool = new AddressPoolImpl()

        and: "expected addition event is defined"
        def expectedAddress = new Address("host4", 4040)
        def expectedEvent = new AddressPoolEvent(AddressPoolEvent.Operation.ADD, expectedAddress)

        and: "pool is monitored"
        def listener = Mock(Observer)
        pool.getChanges().subscribe(listener)

        when: "address is added to the pool"
        def actualAddress = pool.add(expectedAddress.getHostname(), expectedAddress.getPort())

        then: "the pool contains given address"
        actualAddress == expectedAddress
        pool.getAddresses().contains(expectedAddress)

        and: "addition event was emitted"
        1 * listener.onNext(expectedEvent)
    }
}
