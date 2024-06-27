package org.example.proxyclient;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import lombok.Getter;
import org.example.proxyclient.Gui.Logger;
import org.example.proxyclient.Gui.TypeModeHandler;
import org.example.proxyclient.Transfer.MessageTransferObject;
import org.example.proxyclient.Transfer.Payload;
import org.example.proxyclient.Utils.MTOJsonParser;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class FXController implements Initializable {

    @FXML
    private ToggleButton producerToggle, subscriberToggle, createToggle, deleteToggle, postToggle, subscribeToggle, unsubscribeToggle, fileToggle;

    @FXML
    private ToggleGroup modeGroup, producerGroup, subscriberGroup;

    @FXML
    private Button connectButton, disconnectButton, myStatusButton, serverStatusButton, sendButton;

    @FXML
    private TextField idField, ipField, portField, topicField, messageField, messageTopicField, fileField;

    @FXML
    private TextArea logsArea, serverArea, jsonPreview;


    private Logger logger;
    private TypeModeHandler typeModeHandler;

    private ProxyClientManager clientManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger = new Logger(jsonPreview, logsArea, serverArea);
        Logger.setInstance(logger);

        typeModeHandler = new TypeModeHandler(
                producerToggle, subscriberToggle, createToggle, deleteToggle, postToggle,
                subscribeToggle, unsubscribeToggle, fileToggle, modeGroup, producerGroup, subscriberGroup);

        clientManager = new ProxyClientManager(this);
    }

    @FXML
    protected void startConnection() {
        String ip = getIp();
        if (ip == null) {
            logger.log(Logger.ERROR, "Given ip is in wrong format!");
            return;
        }
        Integer port = getPort();
        if (port == -1) {
            logger.log(Logger.ERROR, "Given port is not a number!");
            return;
        }
        if (idField.getText().isEmpty()) {
            logger.log(Logger.ERROR, "Username cannot be empty!");
            return;
        }

        try {
            clientManager.start(ip, port, idField.getText());
        } catch (IOException e) {
            logger.log(Logger.ERROR, e.getMessage());
            return;
        }
        swapGuiStatus();
    }

    @FXML
    protected void stopConnection() {
        clientManager.stop();
        swapGuiStatus();
    }

    @FXML
    protected void send() {
        System.out.println(typeModeHandler.getTypemode());
        clientManager.send();
    }

    @FXML
    protected void myStatus() {
        clientManager.getStatus();
    }

    @FXML
    protected void serverStatus() {
        clientManager.getServerStatus();
    }

    public String getIp () {
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        String ip = ipField.getText();
        Pattern pattern = Pattern.compile(ipPattern);
        Matcher matcher = pattern.matcher(ip);

        if (matcher.matches() || ip.equals("localhost")) {
            return ip;
        } else {
            return null;
        }
    }

    public Integer getPort() {
        Integer port;
        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            return -1;
        }
        return port;
    }

    public void swapGuiStatus() {
        setDisables(connectButton.isDisabled());
    }

    private void setDisables(boolean bool) {
        connectButton.setDisable(!bool);
        disconnectButton.setDisable(bool);
        ipField.setDisable(!bool);
        portField.setDisable(!bool);
        idField.setDisable(!bool);
        topicField.setDisable(bool);
        messageTopicField.setDisable(bool);
        messageField.setDisable(bool);
        fileField.setDisable(bool);

        modeGroup.getToggles().forEach(toggle -> {
            Node n = (Node) toggle;
            n.setDisable(bool);
        });

        producerGroup.getToggles().forEach(toggle -> {
            Node n = (Node) toggle;
            n.setDisable(bool);
        });

        subscriberGroup.getToggles().forEach(toggle -> {
            Node n = (Node) toggle;
            n.setDisable(bool);
        });

        sendButton.setDisable(bool);
        myStatusButton.setDisable(bool);
        serverStatusButton.setDisable(bool);
    }
}