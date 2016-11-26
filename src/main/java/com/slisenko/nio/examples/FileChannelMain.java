package com.slisenko.nio.examples;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelMain {

    public static void main(String[] args) throws IOException {
        RandomAccessFile file = new RandomAccessFile("target/raf.txt", "rw");
        FileChannel channel = file.getChannel();

        // Writing to file
        ByteBuffer bigBuffer = ByteBuffer.allocate(10);
        bigBuffer.put("hello-nio".getBytes());
        bigBuffer.flip(); // If we do not flip - write not happens

        channel.write(bigBuffer);
        channel.force(true);
        channel.position(0);

        // Reading from file
        ByteBuffer smallBuffer = ByteBuffer.allocate(5);

        int bytesRead = channel.read(smallBuffer);
        System.out.println("BytesRead=" + bytesRead + " [" + new String(smallBuffer.array()) + "]");
        smallBuffer.clear();

        bytesRead = channel.read(smallBuffer);
        System.out.println("BytesRead=" + bytesRead + " [" + new String(smallBuffer.array()) + "]");
    }
}