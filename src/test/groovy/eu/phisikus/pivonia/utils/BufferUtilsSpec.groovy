package eu.phisikus.pivonia.utils

import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

class BufferUtilsSpec extends Specification {

    @Shared
    def testBuffer = ByteBuffer
            .allocate(4)
            .put((byte) 57)
            .put((byte) 48)
            .put((byte) 0)
            .put((byte) 0)
            .rewind()

    @Shared
    def expectedSize = 12345

    def "Message size should be extracted from buffer properly"() {
        when: "message size calculation is performed using test buffer"
        def actualSize = BufferUtils.readMessageSizeFromBuffer(testBuffer)
        testBuffer.rewind()

        then: "returned value is equal to expected"
        actualSize == expectedSize

    }

    def "Buffer with message size should be produced properly"() {
        when: "calling for creation of buffer with message size in it"
        def actualBuffer = BufferUtils.getBufferWithMessageSize(expectedSize)

        then: "the returned buffer contains expected data"
        actualBuffer == testBuffer
    }

    def "Message size and content should be placed in a new buffer"() {
        given: "content buffer and expected buffer are defined"
        def contentBuffer = ByteBuffer.allocate(4).putInt(42).rewind()
        def expectedBuffer = ByteBuffer
                .allocate(8)
                .put(testBuffer)
                .put(contentBuffer)
                .rewind()
        testBuffer.rewind()
        contentBuffer.rewind()

        when: "retrieving buffer containing size and message contents"
        def actualBuffer = BufferUtils.getBufferWithCombinedSizeAndContent(expectedSize, contentBuffer)

        then: "the actual buffer contains expected data"
        actualBuffer.array() == expectedBuffer.array()
    }


}
