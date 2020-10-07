package eu.phisikus.pivonia.tcp.utils

import spock.lang.Specification

class LocalAddressResolverSpec extends Specification {
        def "Should determine local address properly"() {
        given: "resolver instance is created"
        def networkAddressResolver = new LocalAddressResolver()

        when: "calling for local address retrieval"
        def actualAddress = networkAddressResolver.getAddress()

        then: "returned address is correct"
        actualAddress.isSuccess()
        verifyAll(actualAddress.get()) {
            InetAddress.getByName(hostname) != null
            port != null
            port > 0
        }
    }
}
