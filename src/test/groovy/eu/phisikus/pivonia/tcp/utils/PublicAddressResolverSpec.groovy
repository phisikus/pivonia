package eu.phisikus.pivonia.tcp.utils

import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class PublicAddressResolverSpec extends Specification {

    def "Should determine public address properly"() {
        given: "client is mocked"
        def client = Mock(HttpClient)
        def response = Mock(HttpResponse)
        def expectedAddress = "127.0.0.200"

        and: "resolver instance is created"
        def providerAddress = "http://localhost"
        def networkAddressResolver = new PublicAddressResolver(client, [providerAddress])

        when: "calling for public address retrieval"
        def actualAddress = networkAddressResolver.getAddress()

        then: "client is called for the address"
        1 * client.send({
            def request = it as HttpRequest
            request.method() == "GET"
            request.uri() == URI.create(providerAddress)
        }, HttpResponse.BodyHandlers.ofString()) >> response
        1 * response.statusCode() >> 200
        1 * response.body() >> expectedAddress

        and: "actual address was determined correctly"
        verifyAll(actualAddress) {
            isSuccess()
            verifyAll(get()) {
                hostname == expectedAddress
                port != null
                port > 0
            }
        }
    }

    def "Should use secondary address provider once primary fails"() {
        given: "client is mocked"
        def client = Mock(HttpClient)
        def expectedAddress = "127.0.0.200"
        def response = Mock(HttpResponse)
        def failingResponse = Mock(HttpResponse)

        and: "resolver instance is created"
        def providerAddress = "http://localhost"
        def failingProviderAddress = "http://externalhost"
        def networkAddressResolver = new PublicAddressResolver(client, [failingProviderAddress, providerAddress])

        when: "calling for public address retrieval"
        def actualAddress = networkAddressResolver.getAddress()

        then: "client calls the first provider"
        1 * client.send({
            def request = it as HttpRequest
            request.method() == "GET"
            request.uri() == URI.create(failingProviderAddress)
        }, HttpResponse.BodyHandlers.ofString()) >> failingResponse
        1 * failingResponse.statusCode() >> 500

        and: "makes second call to different provider"
        1 * client.send({
            def request = it as HttpRequest
            request.method() == "GET"
            request.uri() == URI.create(providerAddress)
        }, HttpResponse.BodyHandlers.ofString()) >> response
        1 * response.statusCode() >> 200
        1 * response.body() >> expectedAddress

        and: "actual address was determined correctly"
        verifyAll(actualAddress) {
            isSuccess()
            verifyAll(get()) {
                hostname == expectedAddress
                port != null
                port > 0
            }
        }
    }

    def "Should provide failure result if all providers fail"() {
        given: "client is mocked"
        def client = Mock(HttpClient)
        def failingProviderAddress = "http://externalhost"

        and: "resolver instance is created"
        def networkAddressResolver = new PublicAddressResolver(client, [failingProviderAddress])

        when: "calling for public address retrieval"
        def actualAddress = networkAddressResolver.getAddress()

        then: "client calls the first provider"
        1 * client.send({
            def request = it as HttpRequest
            request.method() == "GET"
            request.uri() == URI.create(failingProviderAddress)
        }, HttpResponse.BodyHandlers.ofString()) >> { throw new IOException() }

        and: "failure result is returned"
        verifyAll(actualAddress) {
            isFailure()
            getCause().getClass() == NetworkAddressResolver.AddressNotResolvedException.class
        }
    }
}
