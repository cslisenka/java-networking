package com.slisenko.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LoadTestingClient {

    private static boolean isRunning = true;

    public static void main(String[] args) throws InterruptedException, IOException {
        List<Socket> sockets = new ArrayList<Socket>();
        System.out.println("Opening many sockets");
        for (int i = 0; i < 20_000; i++) {
            try {
                sockets.add(new Socket("localhost", 45000));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.print("Writing data to sockets");

        new Thread() {
            public void run() {
                new Scanner(System.in).next();
                isRunning = false;
            }
        }.start();

        // Start writing
        while (isRunning) {
            for (Socket socket : sockets) {
                try {
                    socket.getOutputStream().write(1);
                } catch (IOException e) {} // Never do this
            }
            delay(200);
        }

        // Closing connections
        System.out.print("Closing connections");
        for (Socket socket : sockets) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("error closing socket " + e.getMessage());
            }
        }
    }

    private static void delay(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {} // Never do this
    }
}