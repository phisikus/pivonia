package eu.phisikus.pivonia.middleware

import eu.phisikus.pivonia.middleware.layer.pool.test.FakeMessage
import spock.lang.Subject

class CakeWithClientPoolTest extends CakeIntegrationTest {

    @Subject
    def cake = new CakeWithClientPool(FakeMessage)

    def "Should compose client pool"() {
        expect: "Client pool to be available"
        cake.getClientPool() != null
    }
}
