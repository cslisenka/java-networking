package com.slisenko.server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class NioServerMain {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        // NIO works on top of classic sockets from package (java.net)
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(new InetSocketAddress(45000)); // Port=45000

        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer readBuffer = ByteBuffer.allocate(100);
        readBuffer.clear(); // Ready to read

        ByteBuffer writeBuffer = ByteBuffer.allocate(100);
        writeBuffer.clear(); // Ready to read

        String receivedMessage = "";

        System.out.println("Waiting for incoming connections");
        boolean isExit = false;
        while (!isExit) {
            // Blocks until at least one of the events happens
            int events = selector.select();
            System.out.println("Selected events = " + events);

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    System.out.println("key=acceptable");

                    SocketChannel socketChannel = serverChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println(socketChannel + " accept and register");
                }

                // Read data from remote machine
                if (key.isReadable()) {
                    System.out.println("key=readable");
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    System.out.println(socketChannel + " read");

                    int totalBytesRead = 0;
                    int bytesRead = 0;
                    try {
                        bytesRead = socketChannel.read(readBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                        socketChannel.close();
                        continue;
                    }
                    // If don't safeClose channel - we will get forever loop
                    // Instead of disconnect we need to catch -1 or any IOException
                    if (bytesRead == -1) {
                        // TODO we should also catch exception
                        System.out.println("Client disconnected " + socketChannel.getRemoteAddress());
                        socketChannel.close();
                        // Key can not be used anymore
                        continue;
                    }

                    // We need to handle situation when only part of message is read
                    while (bytesRead > 0) {
                        totalBytesRead += bytesRead;
                        bytesRead = socketChannel.read(readBuffer);
                        System.out.println("Reading " + bytesRead + " bytes...");
                    }

                    System.out.println("Reading finished, bytes=" + totalBytesRead);
                    printBufferDetails(readBuffer);

                    readBuffer.flip();
                    // Reading buffer into String
                    byte[] array = new byte[readBuffer.limit() + 1];
                    readBuffer.get(array, 0, readBuffer.limit());

                    receivedMessage = new String(array);
                    System.out.println("Received: " + receivedMessage);

                    readBuffer.clear();

                    socketChannel.register(selector, SelectionKey.OP_WRITE);
                }

                // Writing data to remote machine
                if (key.isWritable()) {
                    System.out.println("key=writable");
                    SocketChannel socketChannel = (SocketChannel) key.channel();

                    String toSend = "pong" + receivedMessage + '\n';
                    System.out.println("to send=" + toSend);
                    writeBuffer.put(toSend.getBytes());
                    receivedMessage = "";
                    writeBuffer.flip();
                    printBufferDetails(writeBuffer);

                    int bytesWrite = 0;
                    int totalBytesWrite = 0;
                    // We need to handle situation when only part of message is written
                    while ((bytesWrite = socketChannel.write(writeBuffer)) > 0) {
                        totalBytesWrite += bytesWrite;
                        System.out.println("Bytes written = " + bytesWrite);
                    }

                    System.out.println("Writing finished, total=" + totalBytesWrite);
                    printBufferDetails(writeBuffer);
                    writeBuffer.clear().rewind();
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }

                if (key.isConnectable()) {
                    System.out.println("key=connectable");
                }

                iterator.remove();
            }
        }

        serverChannel.close();
    }

    public static void print(ByteBuffer buffer) {
        System.out.println(new String(buffer.array()));
    }

    public static void printBufferDetails(ByteBuffer buf) {
        System.out.println(" Position=" + buf.position() + " Limit=" + buf.limit() +
                " Capacity=" + buf.capacity() + " Remaining=" + buf.remaining());
    }
}