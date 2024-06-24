package org.example.proxyclient.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.example.proxyclient.Transfer.MessageTransferObject;
import org.example.proxyclient.Transfer.Payload;

import java.time.LocalDateTime;

public class MTOJsonParser {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls()
            .setPrettyPrinting()
            .create();


    public static MessageTransferObject parseJsonToMessageTransferObject(String jsonString) throws JsonParseException, IllegalArgumentException {

        MessageTransferObject message = gson.fromJson(jsonString, MessageTransferObject.class);
        validateMessageTransferObject(message);
        return message;
    }

    public static String parseToString(MessageTransferObject message) {
        if (message == null) {
            throw new IllegalArgumentException("MessageTransferObject cannot be null");
        }
        return gson.toJson(message);
    }

    private static void validateMessageTransferObject(MessageTransferObject message) throws IllegalArgumentException {
        if (message.getType() == null) {
            throw new IllegalArgumentException("MessageType is null");
        }
        if (message.getId() == null || message.getId().isEmpty()) {
            throw new IllegalArgumentException("ID is null or empty");
        }
        if (message.getMode() == null) {
            throw new IllegalArgumentException("MessageMode is null");
        }
        if (message.getTimestamp() == null) {
            throw new IllegalArgumentException("Timestamp is null");
        }
        if (message.getPayload() == null) {
            throw new IllegalArgumentException("Payload is null");
        }
        // Validate Payload fields
        Payload payload = message.getPayload();
        if (payload.getTimestampOfMessage() == null) {
            throw new IllegalArgumentException("Timestamp of message is null");
        }
        if (payload.getTopicOfMessage() == null || payload.getTopicOfMessage().isEmpty()) {
            throw new IllegalArgumentException("Topic of message is null or empty");
        }
        if (payload.getMessage() == null || payload.getMessage().isEmpty()) {
            throw new IllegalArgumentException("Message is null or empty");
        }
    }
}

