package com.slisenko.server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Bad code, do not do this
public class PollingNioServer {

    private static final Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(45001));
        serverChannel.configureBlocking(false);

        log("Server started at port 45001. Waiting for connections...");

        while (true) {
            // Non blocking, almost always null
            SocketChannel socketChannel = serverChannel.accept();
            if (socketChannel != null) {
                socketChannel.configureBlocking(false);
                log("Connected " + socketChannel.getRemoteAddress());
                sockets.put(socketChannel, ByteBuffer.allocate(1000)); // Allocating buffer for socket channel
            }

            // Iterating over connected clients
            sockets.forEach((socket, buffer) -> {
                try {
                    int bytesRead = socket.read(buffer); // Reading, non-blocking call
                    if (bytesRead > 0) {
                        log("Reading from " + socket.getRemoteAddress() + ", bytes read=" + bytesRead);
                    }
                    if (bytesRead == -1) { // Connection closed from client side
                        log("Connection closed " + socket.getRemoteAddress());
                        sockets.remove(socket);
                        socket.close();
                    }

                    // TODO detect that input message is not partially received
                    if (bytesRead > 0) {
                        buffer.flip();
                        StringBuilder result = new StringBuilder();
                        for (int i = 0; i < buffer.limit() - 1; i++) {
                            byte c = buffer.get(i);
                            if (c != '\r' && c != '\n') {
                                result.append((char)c);
                            }
                        }
                        result.append(", server time=" + System.currentTimeMillis())
                                .append('\r').append('\n');

                        // Writing response to buffer
                        buffer.clear();
                        buffer.put(ByteBuffer.wrap(result.toString().getBytes()));
                        buffer.flip();

                        while (buffer.hasRemaining()) { // Reading from buffer
                            int bytesWritten = socket.write(buffer);
                            log("Writing to " + socket.getRemoteAddress() + ", bytes written=" + bytesWritten);

                            if (bytesWritten == 0) {
                                buffer.compact();
                                break;
                            }
                        }

                        if (buffer.hasRemaining()) {
                            log("has remaining!!!");
                        }
                    }
                } catch (IOException e) {
                    log("error " + e.getMessage());
                }
            });
       }
    }

    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}