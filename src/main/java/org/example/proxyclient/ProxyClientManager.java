package org.example.proxyclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ProxyClientManager {
    private final FXController c;

    private Client client;
    private PrintWriter out;
    private BufferedReader in;

    public ProxyClientManager(FXController c) {
        this.c = c;
    }

    public void start(String ip, int port, String userId) {
        try {
            Socket s = new Socket(ip, port);
            client = new Client(s);

            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            client.setUserId(userId);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean isConnected() {
        return client.getClientSocket().isConnected();
    }

//    private String getStatus() {
//
//    }
//
//    //callback
//    private Object getServerStatus() {
//
//    }
//
//    //callback
//    private Object getServerLogs() {
//
//    }
//
//    public void createProducer(String topicName) {
//
//    }
//
//    public void sendFile(String topicName, String filePath) {
//
//    }
//
//    public void produce(String topicName, Object payload) {
//
//    }
//
//    public void withdrawProducer(String topicName) {
//
//    }
//
//    //callback
//    public void createSubscriber(String topicName) {
//
//    }
//
//    public void stop() {
//
//    }
}
