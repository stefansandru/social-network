module com.example.social_network {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires spring.security.core;

    opens com.example.social_network to javafx.fxml;
    exports com.example.social_network;
}