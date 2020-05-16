package eu.phisikus.pivonia.api


import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

@EqualsAndHashCode
@ToString
@TupleConstructor(includeFields = true)
class TestMessage {
    private Long timestamp;
    private String topic;
    private String message;
}
