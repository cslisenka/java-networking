package com.slisenko;

import java.io.IOException;
import java.net.Socket;

public class Logger {

    public static void log(String message) {
        System.out.println(Thread.currentThread().getName() + " " + message);
    }

    public static void delay(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
