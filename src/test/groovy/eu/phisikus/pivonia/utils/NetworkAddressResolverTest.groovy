package eu.phisikus.pivonia.utils

import spock.lang.Specification

import java.net.http.HttpClient

class NetworkAddressResolverTest extends Specification {

    def "Should determine local address properly"() {
        given: "resolver instance is created"
        def networkAddressResolver = new NetworkAddressResolver(Mock(HttpClient) as HttpClient, [])

        when: "calling for local address retrieval"
        def actualAddress = networkAddressResolver.getLocalInterfaceAddress()

        then: "returned address is correct"
        InetAddress.getByName(actualAddress) != null
    }
}
