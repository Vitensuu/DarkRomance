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
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received: " + line);
                String[] parts = line.split(":");
                String cmd = parts[0];
                if (cmd.equals("REGISTER") && parts.length == 3) {
                    boolean ok = db.register(parts[1], parts[2]);
                    out.println(ok ? "OK" : "ERROR:Username already exists");
                } else if (cmd.equals("LOGIN") && parts.length == 3) {
                    boolean ok = db.login(parts[1], parts[2]);
                    out.println(ok ? "OK" : "ERROR:Invalid credentials");
                } else {
                    out.println("ERROR:Unknown command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) {}
            System.out.println("Client disconnected");
        }
    }
}
