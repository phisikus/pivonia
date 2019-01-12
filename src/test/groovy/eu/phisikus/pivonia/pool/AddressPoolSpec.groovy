package eu.phisikus.pivonia.pool

import spock.lang.Specification

class AddressPoolSpec extends Specification {
    def "Should store address to pool properly"() {

        given: "empty address pool"
        def pool = new AddressPool()

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
}
