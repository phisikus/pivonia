package eu.phisikus.pivonia.middleware

import eu.phisikus.pivonia.middleware.layer.pool.test.FakeMessage
import spock.lang.Specification
import spock.lang.Subject

class CakeWithClientPoolTest extends Specification {
    @Subject
    def cake = new CakeWithClientPool(FakeMessage)

    def "Should provide client pool"() {
        expect: "Client pool to be available"
        cake.getClientPool() != null
    }

    // TODO add real tests - inherit from other?
}
