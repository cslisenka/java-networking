package com.slisenko.nio.examples;

import java.nio.ByteBuffer;

/**
 * Example of using JAVA NIO buffers.
 * Buffer is a block of memory where we can write data and then read
 * capacity - max size of buffer
 * position - where we are
 * limit - max position we can read, for writing limit=capacity
 */
public class BufferExamples {

    public static void main(String[] args) {
        ByteBuffer buf = ByteBuffer.allocate(50);
        // mark <= position <= limit <= capacity

        // Put data, moves position+5
        buf.put("hello".getBytes());
        printBufferDetails("1, +hello", buf);

        // Moves position +5
        buf.put("world".getBytes());
        printBufferDetails("2, +world", buf);

        // Buffer array is not cleaned
        // position=0
        buf.clear();
        printBufferDetails("3, clear()", buf);

        // Rewrites array starting from 0 position, position=3
        buf.put("AAA".getBytes());
        printBufferDetails("4, +AAA", buf);

        // Reading buffer, moves position +1
        System.out.println(buf.get());
        printBufferDetails("4, get()", buf);

        // Cuts buffer, sets limit to position and then position=0
        // Switches buffer from writing to reading mode
        buf.flip();
        printBufferDetails("5, flip()", buf);

        // Resets position, do not cleans byte array
        buf.clear();
        printBufferDetails("5, clear()", buf);

        // Mark sets pointer to current position=3
        buf.put("123".getBytes());
        buf.mark();
        buf.put("456".getBytes());
        printBufferDetails("6, +123 mark() +456", buf);

        // Resets position back to 3
        buf.reset();
        printBufferDetails("7, reset()", buf);

        // Position=0, mark is cleared, does not change limit
        // We can reread the data again
        buf.rewind();
        printBufferDetails("8, rewind()", buf);
    }

    public static void printBufferDetails(String comment, ByteBuffer buf) {
        System.out.println(comment + " [" + new String(buf.array()) + "]" + " Position=" + buf.position() +
                " Limit=" + buf.limit() + " Capacity=" + buf.capacity() +
                " Remaining=" + buf.remaining() +
                " ArrayOffset=" + buf.arrayOffset() + " IsReadOnly=" + buf.isReadOnly());
    }
}