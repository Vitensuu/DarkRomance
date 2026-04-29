package com.team7.game1.network;

import com.badlogic.gdx.Gdx;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NetworkClient {

    private static final int READ_TIMEOUT_MS = 5000;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private volatile boolean connected;

    public void connect(String host, int port, NetworkCallback callback) {
        disconnect();

        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(READ_TIMEOUT_MS);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
        } catch (IOException exception) {
            connected = false;
            postResponse(callback, "ERROR: " + exception.getMessage());
            return;
        }

        listenerThread = new Thread(() -> listenForSingleResponse(callback), "network-client-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void send(String message) {
        if (!connected || out == null) {
            Gdx.app.log("NetworkClient", "Cannot send message: not connected");
            return;
        }
        out.println(message);
        out.flush();
    }

    public void disconnect() {
        connected = false;
        closeQuietly(in);
        closeQuietly(out);
        closeSocketQuietly(socket);
        in = null;
        out = null;
        socket = null;
        listenerThread = null;
    }

    private void listenForSingleResponse(NetworkCallback callback) {
        try {
            String response = in == null ? null : in.readLine();
            if (response == null || response.trim().isEmpty()) {
                postResponse(callback, "ERROR:Timeout");
                return;
            }
            postResponse(callback, response);
        } catch (SocketTimeoutException timeoutException) {
            postResponse(callback, "ERROR:Timeout");
        } catch (IOException ioException) {
            postResponse(callback, "ERROR: " + ioException.getMessage());
        } finally {
            disconnect();
        }
    }

    private void postResponse(NetworkCallback callback, String response) {
        if (callback == null) {
            return;
        }
        Gdx.app.postRunnable(() -> callback.onResponse(response));
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
            // Ignore close exception during disconnect.
        }
    }

    private void closeSocketQuietly(Socket socketToClose) {
        if (socketToClose == null) {
            return;
        }
        try {
            socketToClose.close();
        } catch (IOException ignored) {
            // Ignore close exception during disconnect.
        }
    }
}
