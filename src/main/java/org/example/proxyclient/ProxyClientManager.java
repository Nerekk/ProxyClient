package org.example.proxyclient;

import javafx.application.Platform;
import org.example.proxyclient.Enums.MessageMode;
import org.example.proxyclient.Enums.MessageType;
import org.example.proxyclient.Gui.Logger;
import org.example.proxyclient.Gui.SelectedTypeMode;
import org.example.proxyclient.Gui.ServerPrinter;
import org.example.proxyclient.Transfer.MessageTransferObject;
import org.example.proxyclient.Transfer.Payload;
import org.example.proxyclient.Utils.MTOJsonParser;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyClientManager {

    private final FXController c;

    private Client client;
    private DataOutputStream out;
    private DataInputStream in;

    private Thread listenerThread;
    private volatile AtomicBoolean running = new AtomicBoolean(false);

    private ProxyConfig config;

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

        running.set(true);
        listenerThread = new Thread(this::listenForMessages);
        listenerThread.start();
        getConfig();
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
            Platform.runLater(() -> ServerPrinter.printInfo(json));

            MessageTransferObject hs = MTOJsonParser.parseJsonToMessageTransferObject(json);

//            Platform.runLater(() -> Logger.getInstance().log(Logger.INFO, hs.getPayload().getMessage()));

            return hs.getType() == MessageType.acknowledge;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listenForMessages() {
        while (running.get() && isConnected()) {
            try {
                String message = in.readUTF();
                Platform.runLater( () ->c.getLogger().log(Logger.INFO, "Got server message!"));

                Platform.runLater(() -> ServerPrinter.printInfo(message));


                if (isDisconnectCall(message)) {
                    c.swapGuiStatus();
                    closeClient();
                    break;
                }

                if (isConfigCall(message)) {
                    setConfig(message);
                }

                if (isFileCall(message)) {
                    saveFile(message);
                }

            } catch (IOException e) {
                criticalStop();
                break;
            }
        }
    }

    private boolean isConfigCall(String message) {
        MessageTransferObject mto = MTOJsonParser.parseJsonToMessageTransferObject(message);
        return mto.getType() == MessageType.config;
    }

    private boolean isFileCall(String message) {
        MessageTransferObject mto = MTOJsonParser.parseJsonToMessageTransferObject(message);
        return mto.getType() == MessageType.file;
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
        } else if (selectedType == MessageType.message) {
            Payload p = new Payload(c.getMessageTopicField().getText(), true, c.getMessageField().getText());
            produce(c.getTopicField().getText(), p);
        } else {
            sendFile(c.getTopicField().getText(), c.getFileField().getText());
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

    public void getServerStatus() {
        MessageTransferObject mto = new MessageTransferObject(MessageType.status, client.getUserId(), "logs", MessageMode.producer);
        mto.setPayload(new Payload(mto.getTimestamp(), "Server status", true, "status"));

        writeToServer(mto);
    }

    private void getConfig() {
        MessageTransferObject mto = new MessageTransferObject(MessageType.config, client.getUserId(), "logs", MessageMode.producer);
        mto.setPayload(new Payload(mto.getTimestamp(), "Config", true, "give me config"));

        writeForConfig(mto);
    }

    private void setConfig(String message) {
        MessageTransferObject mto = MTOJsonParser.parseJsonToMessageTransferObject(message);
        String json = mto.getPayload().getMessage();

        JSONObject con = new JSONObject(json);
        config = new ProxyConfig(con);
    }

    public void createProducer(String topicName) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.register, client.getUserId(), topicName, MessageMode.producer);
        mto.setPayload(new Payload());

        writeToServer(mto);
    }

    public void sendFile(String topicName, String filePath) {
        File file = new File(filePath);
        byte[] fileBytes = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        } catch (FileNotFoundException e) {
            Logger.getInstance().log(Logger.ERROR, "File not found!");
            return;
        } catch (IOException e) {
            Logger.getInstance().log(Logger.ERROR, "Invalid file!");
            return;
        }

        String fileName = file.getName();
        String base64File = Base64.getEncoder().encodeToString(fileBytes);


        MessageTransferObject mto = new MessageTransferObject(MessageType.file, client.getUserId(), topicName, MessageMode.producer);
        Payload p = new Payload(mto.getTimestamp(), fileName, true, base64File);
        mto.setPayload(p);

        writeToServer(mto);
    }

    private void saveFile(String message) {
        MessageTransferObject mto = MTOJsonParser.parseJsonToMessageTransferObject(message);

        String fileName = mto.getPayload().getTopicOfMessage();
        String base64File = mto.getPayload().getMessage();

        byte[] fileBytes = Base64.getDecoder().decode(base64File);

        File file = getUniqueFilename(fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileBytes);
            Logger.getInstance().log(Logger.INFO, "File saved path: \n" + file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            Logger.getInstance().log(Logger.ERROR, "File not found!");
        } catch (IOException e) {
            Logger.getInstance().log(Logger.ERROR, "Invalid file!");
        }
    }

    private static File getUniqueFilename(String fileName) {
        File file = new File("received_" + fileName);
        String baseName = fileName;
        String extension = "";

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        String uniqueFileName = "received_" + baseName;
        file = new File(uniqueFileName + extension);
        int counter = 1;
        while (file.exists()) {
            uniqueFileName = "received_" + baseName + "_" + counter;
            file = new File(uniqueFileName + extension);
            counter++;
        }
        return file;
    }


    public void produce(String topicName, Payload payload) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.message, client.getUserId(), topicName, MessageMode.producer);
        mto.setPayload(payload);

        writeToServer(mto);
    }

    public void withdrawProducer(String topicName) {
        MessageTransferObject mto = new MessageTransferObject(MessageType.withdraw, client.getUserId(), topicName, MessageMode.producer);
        mto.setPayload(new Payload());

        writeToServer(mto);
    }

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

    private void writeForConfig(MessageTransferObject mto) {
        String toSend = MTOJsonParser.parseToString(mto);
        Logger.getInstance().previewJson(toSend);

        try {
            out.writeUTF(toSend);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void writeToServer(MessageTransferObject mto) {
        if (config == null) {
            Platform.runLater(() -> Logger.getInstance().log(Logger.ERROR, "Config is null"));
            return;
        }

        String toSend = MTOJsonParser.parseToString(mto);
        Logger.getInstance().previewJson(toSend);

        if (isMessageTooBig(toSend)) {
            Platform.runLater(() -> Logger.getInstance().log(Logger.ERROR, "The message you want to send is bigger than server size limit!"));
            return;
        }

        try {
            out.writeUTF(toSend);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isMessageTooBig(String message) {
        return message.getBytes().length > config.getSizeLimit();
    }

    public void stop() {
        MessageTransferObject mto = new MessageTransferObject(MessageType.status, client.getUserId(), "Handshake", MessageMode.producer);
        mto.setPayload(new Payload("Disconnect", true, "Dc"));
        String json = MTOJsonParser.parseToString(mto);

        try {
            out.writeUTF(json);

            closeClient();
        } catch (IOException e) {
            Platform.runLater(() -> Logger.getInstance().log(Logger.ERROR, "Stopping exception"));
        }
        Platform.runLater(() -> Logger.getInstance().log(Logger.INFO, "Client stopped."));
    }

    private void criticalStop() {
        try {
            closeClient();
        } catch (IOException e) {
            Platform.runLater(() -> Logger.getInstance().log(Logger.ERROR, "Critical stopping exception"));
        }
        Platform.runLater(() -> Logger.getInstance().log(Logger.INFO, "Critical stopped client."));
    }

    private void closeClient() throws IOException {
        running.set(false);

        in.close();
        out.close();
        client.getClientSocket().close();
    }
}
