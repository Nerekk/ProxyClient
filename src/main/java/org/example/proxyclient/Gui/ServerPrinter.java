package org.example.proxyclient.Gui;

import org.example.proxyclient.Transfer.MessageTransferObject;
import org.example.proxyclient.Utils.MTOJsonParser;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerPrinter {
    public static void printInfo(String message) {
        MessageTransferObject mto = MTOJsonParser.parseJsonToMessageTransferObject(message);

        switch (mto.getType()) {
            case register, reject, withdraw, acknowledge, message, status -> print(mto);
            case file -> printFile(mto);
            case config -> printConfig(mto);
            case null, default -> printUnknown();
        }
    }
    
    private static void print(MessageTransferObject mto) {
        StringBuilder result = getBase(mto);

        String message = "Message: " + mto.getPayload().getMessage();
        result.append(message);

        Logger.getInstance().logServer(result.toString());
    }

    private static StringBuilder getBase(MessageTransferObject mto) {
        StringBuilder result = new StringBuilder();
        String date = formatDate(mto.getTimestamp());
        String header = "[" + mto.getType() + ", " + date + "]\n";

        String topic = "Topic: " + mto.getTopic() + "\n";
        String topicOfMessage = "Subject: " + mto.getPayload().getTopicOfMessage() + "\n";
        result
                .append(header)
                .append(topic)
                .append(topicOfMessage);

        return result;
    }

    private static void printFile(MessageTransferObject mto) {
        StringBuilder result = getBase(mto);
        String message = "Message: You received a file!";

        result.append(message);

        Logger.getInstance().logServer(result.toString());
    }

    private static void printConfig(MessageTransferObject mto) {
        StringBuilder result = getBase(mto);
        String message = "Message: You received a config!\n";

        JSONObject config = new JSONObject(mto.getPayload().getMessage());
        String serverId = config.getString("ServerID");
        int sizeLimit = config.getInt("SizeLimit");
        int timeOut = config.getInt("TimeOut");
        String c = "Server id: " + serverId + "\nServer time out: " + timeOut + "ms\nMessage size limit: " + sizeLimit + "b";

        result.append(message).append(c);

        Logger.getInstance().logServer(result.toString());
    }

    private static void printUnknown() {
        Logger.getInstance().logServer("Got message with unknown type");
    }

    private static String formatDate(LocalDateTime local) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "[" + local.format(formatter) + "]";
    }
}
