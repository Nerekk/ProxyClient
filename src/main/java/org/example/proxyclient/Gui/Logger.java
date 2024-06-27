package org.example.proxyclient.Gui;

import javafx.scene.control.TextArea;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public static final String INFO = "[INFO]";
    public static final String ERROR = "[ERROR]";

    private final TextArea jsonPreview, logs, server;

    private static Logger instance;

    public Logger(TextArea jsonPreview, TextArea logs, TextArea server) {
        this.jsonPreview = jsonPreview;
        this.logs = logs;
        this.server = server;
    }

    public static Logger getInstance() {
        return instance;
    }

    public static void setInstance(Logger logger) {
        instance = logger;
    }


    public synchronized void log(String type, String message) {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTimeString = "[" + currentTime.format(formatter) + "]";
        String currentDebug = logs.getText();

        String info;
        message = type + " " + message;
        if (currentDebug.isEmpty()) {
            info = currentTimeString + message;
        } else {
            info = "\n" + currentTimeString + message;
        }

        logs.appendText(info);
    }

    public void previewJson(String json) {
        jsonPreview.clear();
        jsonPreview.appendText(json);
    }

    public void logServer(String message) {
        String currentDebug = server.getText();

        String info;
        if (currentDebug.isEmpty()) {
            info = message + "\n";
        } else {
            info = "\n" + message + "\n";
        }

        server.appendText(info);
    }

}
