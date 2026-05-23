package com.cireonapp.server.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class InternetConnection {
    public static boolean isConnected() {
        String[] hosts = {"1.1.1.1", "8.8.8.8", "google.com"};

        for (String host : hosts) {
            try (Socket socket = new Socket()) {
                InetSocketAddress address = new InetSocketAddress(host, 80);
                socket.connect(address, 2000); // Timeout in ms
                return true; // Successfully connected
            } catch (IOException ignored) {
            }
        }
        return false; // All hosts failed
    }
}
