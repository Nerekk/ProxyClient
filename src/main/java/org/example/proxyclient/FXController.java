package org.example.proxyclient;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.example.proxyclient.Gui.Logger;
import org.example.proxyclient.Gui.TypeModeHandler;
import org.example.proxyclient.Transfer.MessageTransferObject;
import org.example.proxyclient.Transfer.Payload;
import org.example.proxyclient.Utils.MTOJsonParser;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class FXController implements Initializable {

    @FXML
    private ToggleButton producerToggle, subscriberToggle, createToggle, deleteToggle, postToggle, subscribeToggle, unsubscribeToggle;

    @FXML
    private ToggleGroup modeGroup, producerGroup, subscriberGroup;

    @FXML
    private Button connectButton, disconnectButton, myStatusButton, serverStatusButton, serverLogsButton, sendButton;

    @FXML
    private TextField idField, ipField, portField, topicField, messageField;

    @FXML
    private TextArea logsArea, serverArea, jsonPreview;


    private Logger logger;
    private TypeModeHandler typeModeHandler;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger = new Logger(jsonPreview, logsArea, serverArea);
        Logger.setInstance(logger);

        typeModeHandler = new TypeModeHandler(
                producerToggle, subscriberToggle, createToggle, deleteToggle, postToggle,
                subscribeToggle, unsubscribeToggle, modeGroup, producerGroup, subscriberGroup);
    }

    @FXML
    protected void startConnection() {
        swapGuiStatus();
        Payload payload = new Payload();
        payload.setTimestampOfMessage(LocalDateTime.now());

        MessageTransferObject mto = new MessageTransferObject();
        mto.setTimestamp(LocalDateTime.now());
        mto.setPayload(payload);

        String s = MTOJsonParser.parseToString(mto);
        logger.previewJson(s);
    }

    @FXML
    protected void stopConnection() {
        swapGuiStatus();
    }

    @FXML
    protected void send() {
        System.out.println(typeModeHandler.getTypemode());
    }

    private void swapGuiStatus() {
        setDisables(connectButton.isDisabled());
    }

    private void setDisables(boolean bool) {
        connectButton.setDisable(!bool);
        disconnectButton.setDisable(bool);
        ipField.setDisable(!bool);
        portField.setDisable(!bool);
        idField.setDisable(!bool);
        topicField.setDisable(bool);
        messageField.setDisable(bool);

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
        serverLogsButton.setDisable(bool);
    }
}