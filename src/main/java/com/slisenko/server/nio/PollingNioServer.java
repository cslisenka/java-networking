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
                    if (bytesRead == -1) { // Connection closed from client side
                        log("Connection closed " + socket.getRemoteAddress());
                        sockets.remove(socket);
                        socket.close();
                    }

                    // TODO detect that input message is not partially received
                    if (bytesRead > 0 && buffer.get(buffer.position() - 1) == '\n') {
                        log("Reading from " + socket.getRemoteAddress() + ", bytes read=" + bytesRead);
                        buffer.flip();

                        String clientMessage = new String(buffer.array(), buffer.position(), buffer.limit());
                        String response = clientMessage.replace("\r\n", "") +
                                ", server time=" + System.currentTimeMillis() + "\r\n";

                        // Writing response to buffer
                        buffer.clear();
                        buffer.put(ByteBuffer.wrap(response.getBytes()));
                        buffer.flip();

                        int bytesWritten = socket.write(buffer);
                        log("Writing to " + socket.getRemoteAddress() + ", bytes written=" + bytesWritten);
                        buffer.compact();
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