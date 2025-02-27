// File: src/main/java/com/example/social_network/LoginController.java
    package com.example.social_network;

    import com.example.social_network.Repo.MessageRepo;
    import com.example.social_network.Repo.dbFriendshipRepo;
    import com.example.social_network.Repo.dbUserRepo;
    import com.example.social_network.Validator.FriendshipValidator;
    import com.example.social_network.Validator.UserValidator;
    import com.example.social_network.Service.SocialNetworkService;
    import com.example.social_network.domain.User;
    import com.example.social_network.util.PasswordUtil;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Scene;
    import javafx.scene.control.Label;
    import javafx.scene.control.TextField;
    import javafx.stage.Stage;

    import java.io.IOException;
    import java.util.Objects;

    public class LoginController {

        @FXML
        private Label errorText;

        // Changed: we now use usernameField instead of userIdField.
        @FXML
        private TextField usernameField;

        @FXML
        private TextField passwordField;

        private SocialNetworkService service;

        @FXML
        private void initialize() {
            UserValidator userValidator = new UserValidator();
            FriendshipValidator friendshipValidator = new FriendshipValidator();

            String url = "jdbc:postgresql://localhost:5432/postgres";
            String user = "stefansandru";
            String password = "1234";
            String photosFolder = "/Users/stefansandru/Documents/social_network/ProfileImages";
            dbUserRepo userRepo = new dbUserRepo(userValidator, url, user, password, photosFolder);
            dbFriendshipRepo friendshipRepo = new dbFriendshipRepo(friendshipValidator, url, user, password, photosFolder);
            MessageRepo messageRepo = new MessageRepo(url, user, password, userRepo);

            this.service = new SocialNetworkService(userRepo, friendshipRepo, messageRepo);
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
                showAlert("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @FXML
        private void handleRegister() {
            try {
                String username = usernameField.getText();
                service.addUser(username);
                User user = service.findUserByUsername(username);
                openMainWindow(user.getId(), user.getName(), user.getProfileImagePath());
            } catch (Exception e) {
                showAlert("An error occurred: " + e.getMessage());
                e.printStackTrace();
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