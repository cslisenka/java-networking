package com.slisenko.server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Bad code, do not do this
public class SelectorNioServer {

    private static final Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(45001));
        serverChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        log("Server started at port 45001. Waiting for connections...");

        while (true) {
            // Blocking
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                try {
                    SelectionKey key = keys.next();

                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            SocketChannel socketChannel = serverChannel.accept(); // Non blocking, never null
                            socketChannel.configureBlocking(false);
                            log("Connected " + socketChannel.getRemoteAddress());
                            sockets.put(socketChannel, ByteBuffer.allocate(1000)); // Allocating buffer for socket channel
                            socketChannel.register(selector, SelectionKey.OP_READ);
                        } else if (key.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            ByteBuffer buffer = sockets.get(socketChannel);
                            int bytesRead = socketChannel.read(buffer); // Reading, non-blocking call
                            // Check that we fully read message
                            if (bytesRead > 0 && buffer.get(buffer.position() - 1) == '\n') {
                                log("Reading from " + socketChannel.getRemoteAddress() + ", bytes read=" + bytesRead);
                                socketChannel.register(selector, SelectionKey.OP_WRITE);
                            }

                            if (bytesRead == -1) { // Connection closed from client side
                                log("Connection closed " + socketChannel.getRemoteAddress());
                                sockets.remove(socketChannel);
                                socketChannel.close();
                            }

                        } else if (key.isWritable()) {
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            ByteBuffer buffer = sockets.get(socketChannel);

                            // Reading client message from buffer
                            buffer.flip();
                            StringBuilder result = new StringBuilder();
                            for (int i = 0; i < buffer.limit() - 1; i++) {
                                byte c = buffer.get(i);
                                if (c != '\r' && c != '\n') {
                                    result.append((char) c);
                                }
                            }

                            // Building response
                            result.append(", server time=" + System.currentTimeMillis())
                                .append('\r').append('\n');

                            // Writing response to buffer
                            buffer.clear();
                            buffer.put(ByteBuffer.wrap(result.toString().getBytes()));
                            buffer.flip();

                            while (buffer.hasRemaining()) { // Reading from buffer
                                int bytesWritten = socketChannel.write(buffer);
                                log("Writing to " + socketChannel.getRemoteAddress() + ", bytes written=" + bytesWritten);

                                if (bytesWritten == 0) {
                                    buffer.compact();
                                    break;
                                }
                            }

                            socketChannel.register(selector, SelectionKey.OP_READ);
                        }
                    }
                } catch (IOException e) {
                    log("error " + e.getMessage());
                }

                keys.remove();
            }
       }
    }

    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}