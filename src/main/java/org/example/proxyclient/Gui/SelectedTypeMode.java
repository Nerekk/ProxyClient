package org.example.proxyclient.Gui;

import lombok.Data;
import org.example.proxyclient.Enums.MessageMode;
import org.example.proxyclient.Enums.MessageType;

@Data
public class SelectedTypeMode {
    private MessageMode selectedMode;
    private MessageType selectedType;
}
