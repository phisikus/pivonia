package eu.phisikus.pivonia.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Commonly used ByteBuffer operations have been extracted here
 */
public class BufferUtils {
    public static final int INT_SIZE = 4;

    public static ByteBuffer getBufferForMessageSize() {
        return ByteBuffer.allocate(INT_SIZE);
    }

    public static int readMessageSizeFromBuffer(ByteBuffer buffer) {
        return buffer
                .rewind()
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
    }

    public static ByteBuffer getBufferWithMessageSize(int messageSize) {
        return ByteBuffer
                .allocate(INT_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(messageSize)
                .rewind();
    }

    public static ByteBuffer getBufferWithCombinedSizeAndContent(int messageSize, ByteBuffer contentBuffer) {
        var messageSizeBuffer = getBufferWithMessageSize(messageSize);
        return ByteBuffer.allocate(contentBuffer.limit() + messageSizeBuffer.limit())
                .put(messageSizeBuffer)
                .put(contentBuffer)
                .rewind();
    }
}
