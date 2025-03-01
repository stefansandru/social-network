package com.example.social_network;

import com.example.social_network.repository.MessageRepo;
import com.example.social_network.repository.FriendshipRepo;
import com.example.social_network.repository.UserRepo;
import com.example.social_network.service.SocialNetworkService;
import com.example.social_network.validator.UserValidator;
import com.example.social_network.domain.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleMessage {
    private static final Logger logger = LoggerFactory.getLogger(MultipleMessage.class);

    @FXML
    private TextField messageField;

    @FXML
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    @FXML
    private ObservableList<User> selectedUsers = FXCollections.observableArrayList();

    @FXML
    private TextField searchField;

    @FXML
    private ListView<User> usersListView;

    @FXML ListView<User> selectedUsersListView;

    private Long mainUserId;
    private String mainUsername;
    private String mainProfilePhoto;
    private final SocialNetworkService service;

    public MultipleMessage() {
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
    public void initialize() {
        usersListView.setItems(usersList);
        selectedUsersListView.setItems(selectedUsers);
    }

    public void setUser(Long userId, String username, String profilePhoto) {
        this.mainUserId = userId;
        this.mainUsername = username;
        this.mainProfilePhoto = profilePhoto;
        loadUsers();
    }

    private void loadUsers() {
        List<User> users = service.getUsers(mainUserId);
        usersList.setAll(users);
        usersListView.setItems(usersList);
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        List<User> filteredList = service.findUsersByPrefix(searchText, mainUserId);
        usersList.setAll(filteredList);
        usersListView.setItems(usersList);
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        List<Long> userIds = selectedUsers.stream().map(User::getId).collect(Collectors.toList());
        service.sendMessage(mainUserId, userIds, message, null);
        messageField.clear();
    }

    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Stage stage = (Stage) usersListView.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 950, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            MainController controller = loader.getController();
            controller.setUser(mainUserId, mainUsername, mainProfilePhoto);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load main-view.fxml", e);
        }
    }

    @FXML
    private void handleClickUsers(MouseEvent event) {
        // Add selected users from the main users list to the recipients list
        // Single click selects a user and moves them to the recipients
        if (event.getClickCount() == 1) {
            List<User> selected = usersListView.getSelectionModel().getSelectedItems();

            if (!selected.isEmpty()) {
                selectedUsers.addAll(selected);

                selectedUsersListView.setItems(selectedUsers);
            }
        }
    }

    @FXML
    private void handleClickSelectedUsers(MouseEvent event) {
        // Remove users from the recipients list and return them to the available users list
        // Single click removes the user from recipients
        if (event.getClickCount() == 1) {
            User selectedUser = selectedUsersListView.getSelectionModel().getSelectedItem();

            if (selectedUser != null) {
                selectedUsers.remove(selectedUser);
                usersList.add(selectedUser);

                selectedUsersListView.setItems(selectedUsers);
                usersListView.setItems(usersList);
            }
        }
    }
}
