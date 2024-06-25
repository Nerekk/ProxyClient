package org.example.proxyclient;

import javafx.application.Platform;
import org.example.proxyclient.Enums.MessageMode;
import org.example.proxyclient.Enums.MessageType;
import org.example.proxyclient.Gui.Logger;
import org.example.proxyclient.Gui.SelectedTypeMode;
import org.example.proxyclient.Transfer.MessageTransferObject;
import org.example.proxyclient.Transfer.Payload;
import org.example.proxyclient.Utils.MTOJsonParser;

import javax.security.auth.callback.Callback;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyClientManager {
    private final FXController c;

    private Client client;
    private DataOutputStream out;
    private DataInputStream in;

    private Thread listenerThread;
    private volatile AtomicBoolean running = new AtomicBoolean(false);

    public ProxyClientManager(FXController c) {
        this.c = c;
    }

    public void start(String ip, int port, String userId) throws IOException {
        Socket s = new Socket(ip, port);
        client = new Client(s);

        out = new DataOutputStream(client.getOutputStream());
        in = new DataInputStream(client.getInputStream());
        client.setUserId(userId);

        if (!handshake()) {
            throw new IOException("Could not connect");
        }

        c.getLogger().log(Logger.INFO, "Connected!");

        listenerThread = new Thread(this::listenForMessages);
        running.set(true);
        listenerThread.start();
    }

    private boolean handshake() {
        MessageTransferObject mto = new MessageTransferObject(MessageType.status, client.getUserId(), "Handshake", MessageMode.producer);
        mto.setPayload(new Payload());

        String toSend = MTOJsonParser.parseToString(mto);
        Platform.runLater(() -> Logger.getInstance().previewJson(toSend));

        try {
            out.writeUTF(toSend);
            out.flush();

            String json = in.readUTF();
            MessageTransferObject hs = MTOJsonParser.parseJsonToMessageTransferObject(json);

            Platform.runLater(() -> Logger.getInstance().log(Logger.INFO, hs.getPayload().getMessage()));

            return hs.getType() == MessageType.acknowledge;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listenForMessages() {
        while (running.get() && isConnected()) {
            try {
                Platform.runLater( () ->c.getLogger().log(Logger.INFO, "Waiting for server response.."));

                String message = in.readUTF();
                Platform.runLater( () ->c.getLogger().log(Logger.INFO, "Got server message!"));

                Platform.runLater(() -> c.getLogger().logServer(message));


                if (isDisconnectCall(message)) {
                    c.swapGuiStatus();
                    closeClient();
                    break;
                }

            } catch (IOException e) {
                if (running.get()) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private boolean isDisconnectCall(String message) {
        MessageTransferObject mto = MTOJsonParser.parseJsonToMessageTransferObject(message);
        return mto.getType() == MessageType.status && mto.getPayload().getTopicOfMessage().equals("Disconnect");
    }

    public void send() {
        SelectedTypeMode typeMode = c.getTypeModeHandler().getTypemode();

        if (typeMode.getSelectedMode() == MessageMode.producer) {
            handleProducer(typeMode.getSelectedType());
        } else {
            handleSubscriber(typeMode.getSelectedType());
        }

    }

    private void handleSubscriber(MessageType selectedType) {
        if (selectedType == MessageType.register) {
            createSubscriber(c.getTopicField().getText());
        } else {
            withdrawSubscriber(c.getTopicField().getText());
        }
    }

    private void handleProducer(MessageType selectedType) {
        if (selectedType == MessageType.register) {
            createProducer(c.getTopicField().getText());
        } else if (selectedType == MessageType.withdraw) {
            withdrawProducer(c.getTopicField().getText());
        } else {
            Payload p = new Payload(c.getMessageTopicField().getText(), true, c.getMessageField().getText());
            produce(c.getTopicField().getText(), p);
        }
    }

    public boolean isConnected() {
        return client.getClientSocket().isConnected();
    }

    public void getStatus() {
        MessageTransferObject mto = new MessageTransferObject(MessageType.status, client.getUserId(), "logs", MessageMode.producer);
        mto.setPayload(new Payload(mto.getTimestamp(), "My status", true, "status"));

        writeToServer(mto);
    }

    //callback
    public void getServerStatus() {
        MessageTransferObject mto = new MessageTransferObject(MessageType.status, client.getUserId(), "logs", MessageMode.producer);
        mto.setPayload(new Payload(mto.getTimestamp(), "Server status", true, "status"));

        writeToServer(mto);
    }


//
//    //callback
//    private Object getServerLogs() {
//
//    }
//
    public void createProducer(String topicName) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.register, client.getUserId(), topicName, MessageMode.producer);
        mto.setPayload(new Payload());

        writeToServer(mto);
    }
//
//    public void sendFile(String topicName, String filePath) {
//
//    }
//
    public void produce(String topicName, Payload payload) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.message, client.getUserId(), topicName, MessageMode.producer);
        mto.setPayload(payload);

        writeToServer(mto);
    }
//
    public void withdrawProducer(String topicName) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.withdraw, client.getUserId(), topicName, MessageMode.producer);
        mto.setPayload(new Payload());

        writeToServer(mto);
    }
//
//    //callback
    public void createSubscriber(String topicName) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.register, client.getUserId(), topicName, MessageMode.subscriber);
        mto.setPayload(new Payload());

        writeToServer(mto);
    }

    public void withdrawSubscriber(String topicName) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.withdraw, client.getUserId(), topicName, MessageMode.subscriber);
        mto.setPayload(new Payload());

        writeToServer(mto);
    }

    private void writeToServer(MessageTransferObject mto) {
        String toSend = MTOJsonParser.parseToString(mto);
        Logger.getInstance().previewJson(toSend);

        try {
            out.writeUTF(toSend);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        MessageTransferObject mto = new MessageTransferObject(MessageType.status, client.getUserId(), "Handshake", MessageMode.producer);
        mto.setPayload(new Payload("Disconnect", true, "Dc"));
        String json = MTOJsonParser.parseToString(mto);

        try {
            out.writeUTF(json);

//            listenerThread.join();
            closeClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeClient() throws IOException {
        running.set(false);

        in.close();
        out.close();
        client.getClientSocket().close();
    }
}
