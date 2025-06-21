package com.example.social_network;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.io.IOException;
import java.util.Objects;

import static com.example.social_network.util.PasswordUtil.hashPassword;

public class HelloApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(HelloApplication.class);

    @Override
    public void start(Stage stage) {
        try {
            // Hash a password using BCrypt
            System.out.println(hashPassword("stefan"));

            // Replace the values with your own database connection details and profile images path
            DBConnectionAndProfileImagesPath.INSTANCE.setUrl
                ("jdbc:postgresql://localhost:5432/social_network");
            DBConnectionAndProfileImagesPath.INSTANCE.setUser
                ("stefansandru");
            DBConnectionAndProfileImagesPath.INSTANCE.setPassword
                ("1234"); 
            DBConnectionAndProfileImagesPath.INSTANCE.setPhotosFolder
                ("/Users/stefansandru/Documents/social-network/ProfileImages");
            
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 500);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            stage.setTitle("Social Network");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            logger.error("Failed to start application", e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}