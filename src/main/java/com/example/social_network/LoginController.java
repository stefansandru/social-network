package com.example.social_network;

import com.example.social_network.repository.MessageRepo;
import com.example.social_network.repository.FriendshipRepo;
import com.example.social_network.repository.UserRepo;
import com.example.social_network.validator.UserValidator;
import com.example.social_network.service.SocialNetworkService;
import com.example.social_network.domain.User;
import com.example.social_network.util.PasswordUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);


    @FXML
    private Label errorText;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    private SocialNetworkService service;

    @FXML
    private void initialize() {
        String url = DBConnectionAndProfileImagesPath.INSTANCE.getUrl();
        String user = DBConnectionAndProfileImagesPath.INSTANCE.getUser();
        String password = DBConnectionAndProfileImagesPath.INSTANCE.getPassword();
        String photosFolder = DBConnectionAndProfileImagesPath.INSTANCE.getPhotosFolder();

        UserRepo userRepo = new UserRepo(url, user, password, photosFolder);
        FriendshipRepo friendshipRepo = new FriendshipRepo(url, user, password, photosFolder);
        MessageRepo messageRepo = new MessageRepo(url, user, password, userRepo);

        UserValidator userValidator = new UserValidator();
        this.service = new SocialNetworkService(userRepo, friendshipRepo, messageRepo, userValidator);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String plainPassword = passwordField.getText();

        try {
            User user = service.findUserByUsername(username);
            if (user == null) {
                showAlert("Invalid username!");
            } else if (!PasswordUtil.checkPassword(plainPassword, user.getPassword())) {
                showAlert("Invalid password!");
            } else {
                openMainWindow(user.getId(), user.getName(), user.getProfileImagePath());
            }
        } catch (Exception e) {
            logger.error("Login error for user: {}", username, e);
            showAlert(e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        try {
            service.addUser(username);
            User user = service.findUserByUsername(username);
            openMainWindow(user.getId(), user.getName(), user.getProfileImagePath());
        } catch (Exception e) {
            logger.error("Registration error for username: {}", username, e);
            showAlert(e.getMessage());
        }
    }

    private void showAlert(String message) {
        if (errorText != null) {
            if (message == null || message.isEmpty()) {
                errorText.setVisible(false);
            } else {
                errorText.setText(message);
                errorText.setVisible(true);
            }
        } else {
            System.err.println("Error TextArea is not initialized.");
        }
    }

    private void openMainWindow(Long userId, String username, String profileImagePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 950, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            MainController controller = loader.getController();
            controller.setUser(userId, username, profileImagePath);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showAlert("An error occurred: " + e.getMessage());
        }
    }
}