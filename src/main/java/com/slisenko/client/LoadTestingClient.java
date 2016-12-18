package com.slisenko.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LoadTestingClient {

    public static void main(String[] args) throws InterruptedException, IOException {
        List<Socket> sockets = new ArrayList<Socket>();
        System.out.println("Opening sockets");
        for (int i = 0; i < 10_000; i++) {
            try {
                sockets.add(new Socket("localhost", 45000));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.print("Print any string to exit");
        new Scanner(System.in).next();

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
}