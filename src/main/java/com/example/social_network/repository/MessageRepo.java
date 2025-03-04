package com.example.social_network.repository;

import com.example.social_network.domain.Message;
import com.example.social_network.domain.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageRepo {
    private static final Logger logger = LoggerFactory.getLogger(MessageRepo.class);

    private final String url;
    private final String user;
    private final String password;
    protected final UserRepo userRepo;

    public MessageRepo(String url, String user, String password, UserRepo userRepo) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.userRepo = userRepo;
    }

    public Optional<Message> findOne(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        Message message = null;
        String query = "SELECT * FROM messages WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Long messageId = resultSet.getLong("id");
                    Long fromId = resultSet.getLong("from_user_id");
                    String messageString = resultSet.getString("message");
                    LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                    message = new Message(
                            messageId,
                            userRepo.findOne(fromId).orElse(null),
                            new ArrayList<>(),
                            messageString,
                            date,
                            null);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while findOne Message", e);
        }
        return Optional.ofNullable(message);
    }

    public Iterable<Message> findChat(Long id1, Long id2) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT m.* FROM messages m " +
                       "JOIN message_recipients mr ON m.id = mr.message_id " +
                       "WHERE (m.from_user_id = ? AND mr.to_user_id = ?) " +
                       "OR (m.from_user_id = ? AND mr.to_user_id = ?)";

        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, id1);
            statement.setLong(2, id2);
            statement.setLong(3, id2);
            statement.setLong(4, id1);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Long messageId = resultSet.getLong("id");
                    Long fromId = resultSet.getLong("from_user_id");
                    String messageString = resultSet.getString("message");
                    LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                    Message message = new Message(
                            messageId,
                            userRepo.findOne(fromId).orElse(null),
                            new ArrayList<>(),
                            messageString,
                            date,
                            null);
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while findChat", e);
        }
        return messages;
    }

    public Optional<Message> save(Message message) {
        String query = "INSERT INTO messages (from_user_id, message, date, reply_to) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, message.getFrom().getId());
            statement.setString(2, message.getMessage());
            statement.setTimestamp(3, Timestamp.valueOf(message.getDate()));
            statement.setObject(4, message.getReply() != null ? message.getReply().getId() : null, Types.BIGINT);
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        message.setId(generatedKeys.getLong(1));
                        saveRecipients(message);
                    }
                }
            }
            return Optional.of(message);
        } catch (SQLException e) {
            logger.error("Database error: Message save", e);
        }
        return Optional.empty();
    }

    private void saveRecipients(Message message) throws SQLException {
        String query = "INSERT INTO message_recipients (message_id, to_user_id) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            for (User recipient : message.getTo()) {
                statement.setLong(1, message.getId());
                statement.setLong(2, recipient.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public int getTotalMessagesSent(Long id) {
        String query = "SELECT COUNT(*) FROM messages WHERE from_user_id = ?";
        int count = 0;

        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error: int getTotalMessagesSent", e);
        }
        return count;
    }
}