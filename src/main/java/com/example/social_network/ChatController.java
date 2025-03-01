package com.example.social_network;

import com.example.social_network.repository.MessageRepo;
import com.example.social_network.repository.FriendshipRepo;
import com.example.social_network.repository.UserRepo;
import com.example.social_network.service.SocialNetworkService;
import com.example.social_network.validator.UserValidator;
import com.example.social_network.domain.Message;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ChatController {

    @FXML
    private Label chatWithLabel;

    @FXML
    private ObservableList<Message> messagesList = FXCollections.observableArrayList();

    @FXML
    private TextField messageField;

    @FXML
    private ListView<Message> chatListView;

    private static final Logger logger = LoggerFactory.getLogger(FriendshipRepo.class);

    private Long mainUserId;
    private String mainUsername;
    private String mainProfilePhoto;
    private Long selectedUserId;
    private String selectedUsername;
    private final SocialNetworkService service;
    private Message selectedMessage;

    public ChatController() {
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

    public void setUsers(
            Long mainUserId,
            String mainUsername,
            String mainProfilePhoto,
            Long selectedUserId,
            String selectedUsername) {
        this.mainUserId = mainUserId;
        this.mainUsername = mainUsername;
        this.mainProfilePhoto = mainProfilePhoto;
        this.selectedUserId = selectedUserId;
        this.selectedUsername = selectedUsername;
        initializeChat();
    }

    private void loadMessages() {
        messagesList.clear();
        List<Message> messages = service.getChat(mainUserId, selectedUserId);
        messages.sort(Comparator.comparing(Message::getDate));

        messagesList.addAll(messages);
        chatListView.setItems(messagesList);
        chatListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                    String displayText = "- " + message.getFrom().getName() + " - \n" + message.getMessage();
                    if (message.getReply() != null) {
                        displayText = "- " + message.getFrom().getName() + " - \n" +
                                "(Reply to " + message.getReply().getFrom().getName() + ": " + message.getReply().getMessage() + ")\n" +
                                message.getMessage();
                    }
                    setText(displayText);
                }
            }
        });
    }

    public void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Stage stage = (Stage) chatListView.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 950, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            MainController controller = loader.getController();
            controller.setUser(mainUserId, mainUsername, mainProfilePhoto);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void initializeChat() {
        chatWithLabel.setText("Chat with " + selectedUsername);
        loadMessages();
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            Long reply = selectedMessage != null ? selectedMessage.getId() : null;
            service.sendMessage(mainUserId, List.of(this.selectedUserId), message, reply);
            messageField.clear();
            loadMessages();
            selectedMessage = null;
        }
    }

    @FXML
    private void handleChatListClick() {
        Message clickedMessage = chatListView.getSelectionModel().getSelectedItem();
        if (clickedMessage != null) {
            if (clickedMessage.equals(selectedMessage)) {
                selectedMessage = null;
                chatListView.getSelectionModel().clearSelection();
            } else {
                selectedMessage = clickedMessage;
            }
        }
    }
}