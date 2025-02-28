package com.example.social_network.Repo;

import com.example.social_network.domain.Message;
import com.example.social_network.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageRepoTest {



    //  TODO                         VERIFICA




//
//
//    private MessageRepo messageRepo;
//
//    @Mock
//    private dbUserRepo userRepo;
//
//    private final String url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
//    private final String user = "sa";
//    private final String password = "";
//
//    @BeforeEach
//    void setUp() throws SQLException {
//        MockitoAnnotations.openMocks(this);
//
//        // Set up in-memory database
//        try (Connection connection = DriverManager.getConnection(url, user, password)) {
//            Statement stmt = connection.createStatement();
//
//            // Create tables
//            stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
//                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
//                    "from_user_id BIGINT, " +
//                    "message VARCHAR(1000), " +
//                    "date TIMESTAMP, " +
//                    "reply_to BIGINT)");
//
//            stmt.execute("CREATE TABLE IF NOT EXISTS message_recipients (" +
//                    "message_id BIGINT, " +
//                    "to_user_id BIGINT, " +
//                    "PRIMARY KEY (message_id, to_user_id))");
//
//            // Clear tables before each test
//            stmt.execute("DELETE FROM message_recipients");
//            stmt.execute("DELETE FROM messages");
//        }
//
//        messageRepo = new MessageRepo(url, user, password, userRepo);
//    }
//
//    @Test
//    void findOne_withNullId_returnsEmptyOptional() {
//        // Test branch: id is null
//        Optional<Message> result = messageRepo.findOne(null);
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void findOne_withValidId_returnsMessage() throws SQLException {
//        // Setup: Insert test message
//        User testUser = new User(1L, "test", "test", "test");
//        when(userRepo.findOne(1L)).thenReturn(Optional.of(testUser));
//
//        try (Connection connection = DriverManager.getConnection(url, user, password)) {
//            PreparedStatement ps = connection.prepareStatement(
//                    "INSERT INTO messages (id, from_user_id, message, date, reply_to) VALUES (?, ?, ?, ?, ?)",
//                    Statement.RETURN_GENERATED_KEYS);
//            ps.setLong(1, 1L);
//            ps.setLong(2, 1L);
//            ps.setString(3, "Test message");
//            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
//            ps.setNull(5, Types.BIGINT);
//            ps.executeUpdate();
//        }
//
//        // Test branch: id is valid and message exists
//        Optional<Message> result = messageRepo.findOne(1L);
//        assertTrue(result.isPresent());
//        assertEquals("Test message", result.get().getMessage());
//        assertEquals(testUser, result.get().getFrom());
//    }
//
//    @Test
//    void findOne_withNonExistentId_returnsEmptyOptional() {
//        // Test branch: id doesn't exist in database
//        Optional<Message> result = messageRepo.findOne(999L);
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void findChat_returnsCorrectMessages() throws SQLException {
//        // Setup: Create users
//        User user1 = new User(1L, "user1", "pass1", "User One");
//        User user2 = new User(2L, "user2", "pass2", "User Two");
//        when(userRepo.findOne(1L)).thenReturn(Optional.of(user1));
//        when(userRepo.findOne(2L)).thenReturn(Optional.of(user2));
//
//        // Insert messages and recipients
//        try (Connection connection = DriverManager.getConnection(url, user, password)) {
//            // Message 1: from user1 to user2
//            PreparedStatement ps = connection.prepareStatement(
//                    "INSERT INTO messages (id, from_user_id, message, date, reply_to) VALUES (?, ?, ?, ?, ?)",
//                    Statement.RETURN_GENERATED_KEYS);
//            ps.setLong(1, 1L);
//            ps.setLong(2, 1L);
//            ps.setString(3, "Hi from user1");
//            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
//            ps.setNull(5, Types.BIGINT);
//            ps.executeUpdate();
//
//            ps = connection.prepareStatement(
//                    "INSERT INTO message_recipients (message_id, to_user_id) VALUES (?, ?)");
//            ps.setLong(1, 1L);
//            ps.setLong(2, 2L);
//            ps.executeUpdate();
//
//            // Message 2: from user2 to user1
//            ps = connection.prepareStatement(
//                    "INSERT INTO messages (id, from_user_id, message, date, reply_to) VALUES (?, ?, ?, ?, ?)",
//                    Statement.RETURN_GENERATED_KEYS);
//            ps.setLong(1, 2L);
//            ps.setLong(2, 2L);
//            ps.setString(3, "Hi from user2");
//            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
//            ps.setNull(5, Types.BIGINT);
//            ps.executeUpdate();
//
//            ps = connection.prepareStatement(
//                    "INSERT INTO message_recipients (message_id, to_user_id) VALUES (?, ?)");
//            ps.setLong(1, 2L);
//            ps.setLong(2, 1L);
//            ps.executeUpdate();
//        }
//
//        // Test execution
//        List<Message> messages = (List<Message>) messageRepo.findChat(1L, 2L);
//
//        // Verify results
//        assertEquals(2, messages.size());
//        assertTrue(messages.stream().anyMatch(m -> m.getMessage().equals("Hi from user1")));
//        assertTrue(messages.stream().anyMatch(m -> m.getMessage().equals("Hi from user2")));
//    }
//
//    @Test
//    void save_savesMessageAndRecipients() {
//        // Setup
//        User from = new User(1L, "sender", "pass", "Sender");
//        User to = new User(2L, "receiver", "pass", "Receiver");
//        List<User> recipients = new ArrayList<>();
//        recipients.add(to);
//
//        LocalDateTime now = LocalDateTime.now();
//        Message message = new Message(null, from, recipients, "Test message", now, null);
//
//        // Test execution
//        Optional<Message> result = messageRepo.save(message);
//
//        // Verify
//        assertTrue(result.isPresent());
//        assertNotNull(result.get().getId());
//        assertEquals("Test message", result.get().getMessage());
//    }
//
//    @Test
//    void save_withReplyMessage_savesCorrectly() {
//        // Setup
//        User from = new User(1L, "sender", "pass", "Sender");
//        User to = new User(2L, "receiver", "pass", "Receiver");
//        when(userRepo.findOne(1L)).thenReturn(Optional.of(from));
//        when(userRepo.findOne(2L)).thenReturn(Optional.of(to));
//
//        List<User> recipients = new ArrayList<>();
//        recipients.add(to);
//
//        // Create original message
//        Message originalMessage = new Message(1L, from, recipients, "Original message", LocalDateTime.now(), null);
//
//        // Create reply message
//        Message replyMessage = new Message(null, to, List.of(from), "Reply message", LocalDateTime.now(), originalMessage);
//
//        // Test execution
//        Optional<Message> result = messageRepo.save(replyMessage);
//
//        // Verify
//        assertTrue(result.isPresent());
//        assertNotNull(result.get().getId());
//        assertEquals("Reply message", result.get().getMessage());
//        assertEquals(originalMessage, result.get().getReply());
//    }
//
//    @Test
//    void getTotalMessagesSent_returnsCorrectCount() throws SQLException {
//        // Setup: Insert messages from user 1
//        try (Connection connection = DriverManager.getConnection(url, user, password)) {
//            PreparedStatement ps = connection.prepareStatement(
//                    "INSERT INTO messages (from_user_id, message, date) VALUES (?, ?, ?)");
//
//            // Add 3 messages from user 1
//            for (int i = 0; i < 3; i++) {
//                ps.setLong(1, 1L);
//                ps.setString(2, "Message " + i);
//                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
//                ps.addBatch();
//            }
//
//            // Add 2 messages from user 2
//            for (int i = 0; i < 2; i++) {
//                ps.setLong(1, 2L);
//                ps.setString(2, "Message " + i);
//                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
//                ps.addBatch();
//            }
//
//            ps.executeBatch();
//        }
//
//        // Test execution and verification
//        assertEquals(3, messageRepo.getTotalMessagesSent(1L));
//        assertEquals(2, messageRepo.getTotalMessagesSent(2L));
//        assertEquals(0, messageRepo.getTotalMessagesSent(3L)); // User doesn't exist
//    }
}