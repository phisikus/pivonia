package eu.phisikus.pivonia.utils

import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.ByteOrder

class BufferUtilsTest extends Specification {

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
        when:
        def actualSize = BufferUtils.readMessageSizeFromBuffer(testBuffer)
        testBuffer.rewind()

        then:
        actualSize == expectedSize

    }

    def "Buffer with message size should be produced properly"() {
        when:
        def actualBuffer = BufferUtils.getBufferWithMessageSize(expectedSize)

        then:
        actualBuffer == testBuffer
    }

    def "Message size and content should be placed in a new buffer"() {
        given:
        def contentBuffer = ByteBuffer.allocate(4).putInt(42).rewind()
        def expectedBuffer = ByteBuffer
                .allocate(8)
                .put(testBuffer)
                .put(contentBuffer)
                .rewind()
        testBuffer.rewind()
        contentBuffer.rewind()

        when:
        def actualBuffer = BufferUtils.getBufferWithCombinedSizeAndContent(expectedSize, contentBuffer)

        then:
        actualBuffer.array() == expectedBuffer.array()
    }


}
