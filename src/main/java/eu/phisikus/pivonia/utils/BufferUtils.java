package eu.phisikus.pivonia.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BufferUtils {
    public static final int INT_SIZE = 4;

    public static ByteBuffer getBufferForMessageSize() {
        return ByteBuffer.allocate(INT_SIZE);
    }

    public static int readMessageSizeFromBufer(ByteBuffer buffer) {
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

        return ByteBuffer.allocate(messageSize + INT_SIZE)
                .put(messageSizeBuffer)
                .put(contentBuffer)
                .rewind();
    }
}
