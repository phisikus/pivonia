package eu.phisikus.pivonia.pool.mediators


import eu.phisikus.pivonia.pool.AddressPool
import eu.phisikus.pivonia.pool.ClientPool
import io.reactivex.subjects.PublishSubject
import spock.lang.Ignore
import spock.lang.Specification

import javax.inject.Provider

class ClientGeneratorSpec extends Specification {

    @Ignore
    def "Should successfully generate client"() {
        given: "Client Pool and Address Pool are provided"
        def clientPool = Mock(ClientPool)
        def addressPool = Mock(AddressPool)
        def provider = Mock(Provider)

        and: "Address Pool is configured"
        def clientChanges = PublishSubject.create()
        clientPool.getClientChanges() >> clientChanges

        when: "ClientGenerator is created"
        def generator = new ClientGenerator(clientPool, addressPool, provider)




    }
}
