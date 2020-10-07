package eu.phisikus.pivonia.tcp.utils

import spock.lang.Specification

class AvailablePortProviderSpec extends Specification {

    def "Should provide available port number"() {
        expect: "correct number to be returned"
        verifyAll(AvailablePortProvider.getRandomPort()) {
            isSuccess()
            get() != null
            get() > 0
        }
    }
}
