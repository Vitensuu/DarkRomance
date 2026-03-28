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
                System.out.println("Received: " + inputLine);  // обязательно
                String[] parts = inputLine.split(":");
                String command = parts[0];
                if (command.equals("REGISTER") && parts.length == 3) {
                    boolean ok = db.register(parts[1], parts[2]);
                    String response = ok ? "OK" : "ERROR:Username already exists";
                    System.out.println("Sending: " + response);
                    out.println(response);
                } else if (command.equals("LOGIN") && parts.length == 3) {
                    boolean ok = db.login(parts[1], parts[2]);
                    String response = ok ? "OK" : "ERROR:Invalid credentials";
                    System.out.println("Sending: " + response);
                    out.println(response);
                } else {
                    out.println("ERROR:Unknown command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
