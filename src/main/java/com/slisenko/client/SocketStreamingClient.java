package com.slisenko.client;

import com.slisenko.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

public class SocketStreamingClient {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: host port");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        final Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port));
            Logger.log("Connected to server " + socket.getRemoteSocketAddress());

            // Message receiving in separate thread
            new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        while (!socket.isClosed()) {
                            String response = reader.readLine(); // Blocking call
                            Logger.log("received > " + response);
//                            if (response == null) {
//                                break;
//                            }
                        }
                    } catch (IOException e) {
                        Logger.log("Error receiving data from server " + e.getMessage());
                    }
                }
            }.start();

            // Using current thread for message sending
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            Scanner console = new Scanner(System.in);
            while (!socket.isClosed()) {
                Logger.log("Enter message: ");
                String message = console.next();
                String request = message + ", client time=" + new Date().getTime();
                writer.println(request); // Blocking call
                writer.flush();
                Logger.log("send > " + request);

                if (message.contains("disconnect")) {
                    Logger.log("closing socket");
                    socket.close();
                }
            }
       } finally {
            socket.close();
       }
    }
}