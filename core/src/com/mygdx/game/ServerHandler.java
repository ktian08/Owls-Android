package com.mygdx.game;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ServerHandler {

    private Socket socket;

    public void connectSocket() {
        try {
            socket = IO.socket("http://192.168.1.107:8080");
            socket.connect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
