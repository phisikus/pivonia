package eu.phisikus.pivonia.middleware.layer

import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.api.middleware.MiddlewareClient
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import spock.lang.Specification
import spock.lang.Subject

class LoggerLayerSpec extends Specification {

    final logger = Mock(Logger)
    final logLevel = Level.INFO

    @Subject
    def loggerMiddleware = new LoggerLayer<TestMessage>(logger, logLevel)

    def "Should initialize middleware properly"() {
        expect: "the initialization to finish without errors"
        loggerMiddleware.initialize(Mock(MiddlewareClient))
    }

    def "Should close the middleware without error"() {
        expect: "the close method to finish without exception"
        loggerMiddleware.close()
    }

    def "Should log incoming message"() {
        given: "there is a message"
        def testMessage = Mock(TestMessage)

        when: "calling the incoming message handler"
        loggerMiddleware.handleIncomingMessage(testMessage)

        then: "the message is logged"
        1 * testMessage.toString()
        1 * logger.log(logLevel, _)
    }

    def "Should log outgoing message"() {
        given: "there is a message"
        def testMessage = Mock(TestMessage)

        when: "calling the outgoing message handler"
        loggerMiddleware.handleOutgoingMessage(testMessage)

        then: "the message is logged"
        1 * testMessage.toString()
        1 * logger.log(logLevel, _)
    }


}
