package org.example.proxyclient.Transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Payload {
    private LocalDateTime timestampOfMessage;
    private String topicOfMessage;
    private boolean success;
    private String message;

    public Payload(String topicOfMessage, boolean success, String message) {
        this.timestampOfMessage = LocalDateTime.now();
        this.topicOfMessage = topicOfMessage;
        this.success = success;
        this.message = message;
    }

    public Payload() {
        this.timestampOfMessage = LocalDateTime.now();
        this.topicOfMessage = "";
        this.success = true;
        this.message = "";
    }
}
