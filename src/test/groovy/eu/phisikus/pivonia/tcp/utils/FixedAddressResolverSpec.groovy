package eu.phisikus.pivonia.tcp.utils

import eu.phisikus.pivonia.pool.address.Address
import spock.lang.Specification

class FixedAddressResolverSpec extends Specification {

    def "Should return predefined address when asked to resolve"() {
        given: "test address is defined"
        def address = new Address("host-1", 2048)

        and: "resolver instance is created"
        def resolver = new FixedAddressResolver(address)

        when: "calling resolver for address"
        def actualResult = resolver.getAddress()

        then: "fixed address is returned"
        verifyAll(actualResult) {
            actualResult.isSuccess()
            actualResult.get() == address
        }
    }

    def "Should not allow for setting null address on creation"() {
        when: "trying to create resolver instance with null address"
        def resolver = new FixedAddressResolver(null)

        then: "exception is thrown"
        thrown IllegalArgumentException
    }
}
