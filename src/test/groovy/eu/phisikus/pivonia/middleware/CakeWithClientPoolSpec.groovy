package eu.phisikus.pivonia.middleware

import eu.phisikus.pivonia.api.EmptyEnvelope
import spock.lang.Subject

class CakeWithClientPoolSpec extends CakeITSpec {

    @Subject
    def cake = new CakeWithClientPool(EmptyEnvelope)

    def "Should compose client pool"() {
        expect: "client pool to be available"
        cake.getClientPool() != null
    }
}
