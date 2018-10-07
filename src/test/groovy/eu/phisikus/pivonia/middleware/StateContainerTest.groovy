package eu.phisikus.pivonia.middleware

import spock.lang.Specification
import spock.lang.Subject

class StateContainerTest extends Specification {
    @Subject
    def stateContainer = StateContainer.empty()

    def "State container should store given object"() {
        given: "sample object is created"
        def sample = new Dummy()

        when: "container is used to store object"
        stateContainer.set(Dummy, sample)

        then: "the object should be retrievable"
        stateContainer.get(Dummy) == Optional.of(sample)

    }

    private class Dummy {
    }
}
