package com.team7.game1.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    private static final int PORT = 12345;
    private static DatabaseService db = new DatabaseService();

    public static void main(String[] args) {
        System.out.println("Server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket, db)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
}
