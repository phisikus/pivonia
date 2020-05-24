package eu.phisikus.pivonia.it

import eu.phisikus.pivonia.it.mutualexclusion.RicartAgrawalaNode
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.stream.Collectors

class NodeMutualExclusionITSpec extends Specification {

    def polling = new PollingConditions(delay: 1, timeout: 10)

    def "Should spawn multiple nodes that execute critical section"() {
        given: "there are multiple nodes"
        def idList = [0, 1, 2, 3, 4]
        def nodes = idList.stream()
                .map { new RicartAgrawalaNode(it, idList) }
                .collect(Collectors.toList())

        and: "each nodes address pool is filled with location of other nodes"
        def nodesPorts = nodes.stream()
                .map { it.serverPort }
                .collect(Collectors.toList())
        nodes.forEach { node ->
            nodesPorts.forEach {
                node.node.connectionManager.addressPool.add("localhost", it)
            }
        }

        when: "all of them request access to critical section"
        nodes.forEach { it.requestAccess() }

        then: "eventually all of the nodes will enter the critical section"
        polling.eventually {
            nodes.forEach {
                node -> assert node.isSuccess()
            }
        }

        cleanup:
        nodes.forEach { node -> node.dispose() }
    }
}
