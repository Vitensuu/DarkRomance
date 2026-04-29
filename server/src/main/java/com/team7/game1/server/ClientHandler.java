package com.team7.game1.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DatabaseService db;

    public ClientHandler(Socket socket, DatabaseService db) {
        this.socket = socket;
        this.db = db;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Client connected: " + socket.getInetAddress());

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                String[] parts = inputLine.split(":");
                String command = parts[0];

                if (command.equals("LOGIN") && parts.length == 2) {
                    String username = parts[1];
                    boolean ok = db.loginOrCreate(username);
                    out.println(ok ? "OK" : "ERROR:Failed to login");
                } else {
                    out.println("ERROR:Unknown command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {}
            System.out.println("Client disconnected");
        }
    }
}
