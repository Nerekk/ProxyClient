package org.example.proxyclient.Transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.proxyclient.Enums.MessageMode;
import org.example.proxyclient.Enums.MessageType;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageTransferObject {

    private MessageType type;

    private String id;

    private String topic;

    private MessageMode mode;

    private LocalDateTime timestamp;

    private Payload payload;

    public MessageTransferObject(MessageType type, String id, String topic, MessageMode mode) {
        this.type = type;
        this.id = id;
        this.topic = topic;
        this.mode = mode;
        this.timestamp = LocalDateTime.now();
    }
}
