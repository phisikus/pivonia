package eu.phisikus.pivonia.it

import groovy.transform.Immutable

import java.util.concurrent.atomic.AtomicLong

class RicartAgrawalaNode {

/**
 * TODO
 * start - loop of requestForCriticalSection()
 * 1. for each transmitter send(Request)
 *
 * handler onRequest() - if request is better than our current - sendAccept, if not add to waiting list and ignore
 * handler onAccept() - add request to list of approvals - if all are collected, execute CS, later send accept to all pending requests
 *
 */

    class State {
        AtomicLong clock = new AtomicLong(0L)
        boolean isInCriticalSection = false
        List<Request> pendingRequests = []
    }

    @Immutable
    class Request {
        long senderId
        long timestamp
        def isBetterThan(Request other) {
            def otherId = other.senderId
            def thisId = this.senderId
            return timestamp < other.timestamp || (timestamp == other.timestamp && thisId.compareTo(otherId) < 0)
        }
    }

    class Accept {
        long senderId
    }

}
