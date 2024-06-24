package org.example.proxyclient;

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

public class ProxyClientManager {
    private final FXController c;

    private Client client;
    private DataOutputStream out;
    private DataInputStream in;

    private Thread listenerThread;

    public ProxyClientManager(FXController c) {
        this.c = c;
    }

    public void start(String ip, int port, String userId) throws IOException {
        Socket s = new Socket(ip, port);
        client = new Client(s);

        out = new DataOutputStream(client.getOutputStream());
        in = new DataInputStream(client.getInputStream());
        client.setUserId(userId);

        c.getLogger().log(Logger.INFO, "Connected!");

        listenerThread = new Thread(this::listenForMessages);
        listenerThread.start();
    }

    public void listenForMessages() {
        while (isConnected()) {
            try {
                c.getLogger().log(Logger.INFO, "Waiting for server response..");
                String message = in.readUTF();
                c.getLogger().log(Logger.INFO, "Got server message!");

//                MessageTransferObject mto = MTOJsonParser.parseJsonToMessageTransferObject(message);

                c.getLogger().logServer(message);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    client.getClientSocket().close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    break;
                }
                break;
            }
        }
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

    private void getStatus() {

    }

    //callback
    public void getServerStatus() {
        MessageTransferObject mto = new MessageTransferObject(MessageType.status, client.getUserId(), "logs", MessageMode.producer);
        mto.setPayload(new Payload());

        String toSend = MTOJsonParser.parseToString(mto);
        Logger.getInstance().previewJson(toSend);

        try {
            out.writeUTF(toSend);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        String toSend = MTOJsonParser.parseToString(mto);
        Logger.getInstance().previewJson(toSend);

        try {
            out.writeUTF(toSend);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//
//    public void sendFile(String topicName, String filePath) {
//
//    }
//
    public void produce(String topicName, Payload payload) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.message, client.getUserId(), topicName, MessageMode.producer);
        mto.setPayload(payload);

        String toSend = MTOJsonParser.parseToString(mto);
        Logger.getInstance().previewJson(toSend);

        try {
            out.writeUTF(toSend);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//
    public void withdrawProducer(String topicName) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.withdraw, client.getUserId(), topicName, MessageMode.producer);
        mto.setPayload(new Payload());

        String toSend = MTOJsonParser.parseToString(mto);
        Logger.getInstance().previewJson(toSend);

        try {
            out.writeUTF(toSend);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//
//    //callback
    public void createSubscriber(String topicName) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.register, client.getUserId(), topicName, MessageMode.subscriber);
        mto.setPayload(new Payload());

        String toSend = MTOJsonParser.parseToString(mto);
        Logger.getInstance().previewJson(toSend);

        try {
            out.writeUTF(toSend);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void withdrawSubscriber(String topicName) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.withdraw, client.getUserId(), topicName, MessageMode.subscriber);
        mto.setPayload(new Payload());

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
        listenerThread.interrupt();
        try {
            in.close();
            out.close();
            client.getClientSocket().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
