package com.team7.game1.network;

import com.badlogic.gdx.Gdx;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NetworkClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private volatile boolean connected = false;
    private volatile boolean waitingResponse = false;

    public void connect(String host, int port, NetworkCallback callback) {
        try {
            System.out.println("Connecting to " + host + ":" + port);
            System.out.println("Connected, out created: " + out);
            socket = new Socket(host, port);
            socket.setSoTimeout(5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            System.out.println("Connected");
        } catch (IOException e) {
            System.out.println("Connection failed: " + e.getMessage());
            callback.onResponse("ERROR: " + e.getMessage());
            return;
        }

        waitingResponse = true;
        listenerThread = new Thread(() -> {
            try {
                String response;
                while (connected && (response = in.readLine()) != null) {
                    System.out.println("Received: " + response);
                    final String res = response;
                    Gdx.app.postRunnable(() -> {
                        callback.onResponse(res);
                        waitingResponse = false;
                    });
                    break;
                }
                if (waitingResponse) {
                    System.out.println("No response within timeout");
                    Gdx.app.postRunnable(() -> callback.onResponse("ERROR:Timeout"));
                    waitingResponse = false;
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Socket timeout");
                Gdx.app.postRunnable(() -> callback.onResponse("ERROR:Timeout"));
            } catch (IOException e) {
                System.out.println("IO Exception: " + e.getMessage());
                Gdx.app.postRunnable(() -> callback.onResponse("ERROR: " + e.getMessage()));
            } finally {
                disconnect();
            }
        });
        listenerThread.start();
    }

    public void send(String message) {
        if (out != null && connected) {
            System.out.println("Sending: " + message);
            out.println(message);
            out.flush(); // принудительно сбросить буфер
        } else {
            System.out.println("Cannot send, not connected");
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
