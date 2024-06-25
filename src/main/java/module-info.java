module org.example.proxyclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires com.google.gson;
    requires org.json;


    opens org.example.proxyclient to javafx.fxml;
    exports org.example.proxyclient;
    exports org.example.proxyclient.Gui;
    opens org.example.proxyclient.Gui to javafx.fxml;

    exports org.example.proxyclient.Transfer;
    exports org.example.proxyclient.Enums;
    opens org.example.proxyclient.Enums to com.google.gson;
    opens org.example.proxyclient.Transfer to com.google.gson;
}