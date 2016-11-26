package com.slisenko.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

public class SocketRequestResponseClient {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: host port message");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String message = args[2];

        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port));

            // Send message
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.println(message + ", client time=" + new Date().getTime()); // Blocking call
            writer.flush();
            System.out.println("send: " + message);

            // Receive response
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine(); // Blocking call
            System.out.println("received: " + response);
        } finally {
            socket.close();
        }
    }
}