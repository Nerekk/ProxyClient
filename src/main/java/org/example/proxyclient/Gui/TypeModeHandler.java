package org.example.proxyclient.Gui;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import lombok.Getter;
import lombok.Setter;
import org.example.proxyclient.Enums.MessageMode;
import org.example.proxyclient.Enums.MessageType;

@Getter
@Setter
public class TypeModeHandler {
    private final ToggleButton producerToggle, subscriberToggle, createToggle, deleteToggle, postToggle, subscribeToggle, unsubscribeToggle;

    private final ToggleGroup modeGroup, producerGroup, subscriberGroup;

    private SelectedTypeMode typemode;


    public TypeModeHandler(ToggleButton producerToggle, ToggleButton subscriberToggle, ToggleButton createToggle, ToggleButton deleteToggle, ToggleButton postToggle, ToggleButton subscribeToggle, ToggleButton unsubscribeToggle, ToggleGroup modeGroup, ToggleGroup producerGroup, ToggleGroup subscriberGroup) {
        this.producerToggle = producerToggle;
        this.subscriberToggle = subscriberToggle;
        this.createToggle = createToggle;
        this.deleteToggle = deleteToggle;
        this.postToggle = postToggle;
        this.subscribeToggle = subscribeToggle;
        this.unsubscribeToggle = unsubscribeToggle;
        this.modeGroup = modeGroup;
        this.producerGroup = producerGroup;
        this.subscriberGroup = subscriberGroup;

        this.typemode = new SelectedTypeMode();

        setListeners();
        checkButtons();
    }

    public void setListeners() {
        modeGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> checkButtons());
        producerGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> checkButtons());
        subscriberGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> checkButtons());
    }

    public void checkButtons() {
        if (producerToggle.isSelected()) {
            typemode.setSelectedMode(MessageMode.producer);
            checkProducer();
        } else {
            typemode.setSelectedMode(MessageMode.subscriber);
            checkSubscriber();
        }
    }

    private void checkProducer() {
        if (createToggle.isSelected()) {
            typemode.setSelectedType(MessageType.register);
        } else if (deleteToggle.isSelected()) {
            typemode.setSelectedType(MessageType.withdraw);
        } else {
            typemode.setSelectedType(MessageType.message);
        }
    }

    private void checkSubscriber() {
        if (subscribeToggle.isSelected()) {
            typemode.setSelectedType(MessageType.register);
        } else {
            typemode.setSelectedType(MessageType.withdraw);
        }
    }
}
