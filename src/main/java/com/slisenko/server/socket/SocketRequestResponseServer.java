package com.slisenko.server.socket;

import com.slisenko.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketRequestResponseServer {

    public static void main(String[] args) throws IOException {
        // Thread pool for processing incoming requests
        ExecutorService executor = Executors.newFixedThreadPool(10);
        ServerSocket serverSocket = new ServerSocket();

        try {
            // Using port 45000
            serverSocket.bind(new InetSocketAddress("localhost", 45000));
            Logger.log("Request-Response server started at port 45000. Listening client connections...");

            while (true) { // Forever loop for receiving client connections
                // Client connection
                final Socket socket = serverSocket.accept();
                Logger.log("client connected: " + socket.getRemoteSocketAddress());

                // Processing request in new thread
                executor.submit(new Runnable() {
                    public void run() {
                        try {
                            // Receive request from client
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String request = reader.readLine(); // Blocking call

                            Logger.log("received from " + socket.getRemoteSocketAddress() + " > " + request);

                            // Send response
                            String response = request + ", server time=" + new Date().getTime();
                            PrintWriter writer = new PrintWriter(socket.getOutputStream());
                            writer.println(response); // Blocking call
                            writer.flush();

                            Logger.log("send to " + socket.getRemoteSocketAddress() + " > " + response);
                        } catch (IOException e) {
                            Logger.log("error communicating with client " + e.getMessage());
                        } finally {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Logger.log("error closing socket " + e.getMessage());
                            }
                        }
                    }
                });
            }
        } finally {
            executor.shutdown();
            serverSocket.close();
        }
    }
}