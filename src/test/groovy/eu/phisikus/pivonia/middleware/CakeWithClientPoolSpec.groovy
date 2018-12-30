package eu.phisikus.pivonia.middleware

import eu.phisikus.pivonia.middleware.layer.pool.test.FakeMessage
import spock.lang.Subject

class CakeWithClientPoolSpec extends CakeITSpec {

    @Subject
    def cake = new CakeWithClientPool(FakeMessage)

    def "Should compose client pool"() {
        expect: "client pool to be available"
        cake.getClientPool() != null
    }
}
