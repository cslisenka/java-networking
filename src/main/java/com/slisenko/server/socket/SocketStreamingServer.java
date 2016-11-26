package com.slisenko.server.socket;

import com.slisenko.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketStreamingServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();

        try {
            // Using port 45001
            serverSocket.bind(new InetSocketAddress("localhost", 45001));
            System.out.println("Streaming server started at port 45001. Listening client connections...");

            while (true) {
                final Socket socket = serverSocket.accept();

                // Receiving thread
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            while (!socket.isClosed()) {
                                String request = reader.readLine(); // Blocking call
                                Logger.log("received from " + socket.getRemoteSocketAddress() + " > " + request);
                                if (request.contains("disconnect")) {
                                    Logger.log("closing socket");
                                    socket.close();
                                }
                            }
                        } catch (IOException e) {
                            Logger.log("Error receiving data " + e.getMessage());
                        }
                    }
                }.start();

                // Sending thread
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            PrintWriter writer = new PrintWriter(socket.getOutputStream());
                            while (!socket.isClosed()) {
                                String message = "response for " + socket.getRemoteSocketAddress()
                                        + ", server time=" + System.currentTimeMillis();
                                writer.println(message); // Blocking call
                                writer.flush();
                                Logger.log("send to " + socket.getRemoteSocketAddress() + " > " + message);
                                Logger.delay(5000);
                            }
                        } catch (Exception e) {
                            Logger.log("error sending data " + e.getMessage());
                        }
                    }
                }.start();
            }
        } finally {
            serverSocket.close();
        }
    }
}