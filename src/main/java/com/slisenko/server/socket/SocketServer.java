package com.slisenko.server.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {

    public static void main(String[] args) throws IOException {
        // Using pool we need semaphore to reject too many connections
        // Semaphore - block or respond "server hot available"
        // If no, it is easy to DDoS us
        ExecutorService pool = Executors.newFixedThreadPool(200); // Request processing pool
        ServerSocket serverSocket = new ServerSocket(45000);
        log("Server started at port 45000. Listening client connections...");

        try {
            while (true) {
                // Blocking call, never null
                final Socket socket = serverSocket.accept();

//                handle(socket); // Handling in same thread, only one client is served
//                pool.submit(() -> handle(socket)); // Handling in thread pool

                // Having threads it is easy to DDoS us because too many threads are created
                new Thread(() -> handle(socket)).start(); // Handling always in new thread
            }
        } finally {
//            pool.shutdown();
            serverSocket.close();
        }
    }

    private static void handle(Socket socket) {
        log("client connected: " + socket.getRemoteSocketAddress());
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Receive message from client
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String request = reader.readLine(); // Blocking call

            log("receive from " + socket.getRemoteSocketAddress() + " > " + request);

            // Send response
            String response = request + ", servertime=" + System.currentTimeMillis();
            PrintWriter writer = new PrintWriter(out);
            writer.println(response); // Blocking call
            writer.flush();

            log("send to " + socket.getRemoteSocketAddress() + " > " + response);
        } catch (IOException e) {
            log("error " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log("error closing socket " + e.getMessage());
            }
        }
    }

    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}