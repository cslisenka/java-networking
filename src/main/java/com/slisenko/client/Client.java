package com.slisenko.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class Client {

    public static void main(String... args) throws IOException {
        if (args.length < 3) {
            log("Usage: host port message");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String message = args[2];

        Socket socket = new Socket(host, port);
        try {
            // Send message to server
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.println(message + ", client time=" + new Date().getTime()); // Blocking call
            writer.flush();
            log("send > " + message);

            // Receive response from server
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine(); // Blocking call
            log("received < " + response);
        } finally {
            socket.close();
        }
    }

    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }

    private static void delay(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {} // Never do this
    }
}