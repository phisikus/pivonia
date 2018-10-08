package eu.phisikus.pivonia.middleware

import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.api.middleware.Middleware
import spock.lang.Specification

class CakeTest extends Specification {

    def "Should initialize all of its layers"() {

        given: "there are two layers of middleware and empty cake"
        def firstMiddleware = Mock(Middleware)
        def secondMiddleware = Mock(Middleware)
        def cake = new Cake(TestMessage)

        and: "layers are configured to report initialization"
        1 * firstMiddleware.initialize(_)
        1 * secondMiddleware.initialize(_)

        when: "layers are added to the cake"
        cake
                .addLayer(firstMiddleware)
                .addLayer(secondMiddleware)


        then: "they are initialized with the whole cake"
        cake.initialize()
    }

    def "Should throw an exception if layer of dependency is missing"() {

        given: "there is a cake with layer of middleware"
        def cake = new Cake(TestMessage)
        def firstMiddleware = Mock(Middleware)
        cake.addLayer(firstMiddleware)

        and: "that one layer has unmet dependency"
        1 * firstMiddleware.initialize(_) >> { throw new MissingMiddlewareException(String.class) }

        when: "cake is initialized"
        cake.initialize()


        then: "exception is thrown"
        thrown(MissingMiddlewareException)
    }

    // TODO Add integration test for Cake

}
