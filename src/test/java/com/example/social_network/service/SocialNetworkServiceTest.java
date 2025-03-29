package com.example.social_network.service;

import com.example.social_network.repository.MessageRepo;
import com.example.social_network.repository.FriendshipRepo;
import com.example.social_network.repository.UserRepo;
import com.example.social_network.domain.Friendship;
import com.example.social_network.domain.Message;
import com.example.social_network.domain.Tuple;
import com.example.social_network.domain.User;
import com.example.social_network.paging.Page;
import com.example.social_network.paging.Pageable;
import com.example.social_network.validator.UserValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SocialNetworkServiceTest {

    private UserRepo userRepo;
    private FriendshipRepo friendshipRepo;
    private MessageRepo messageRepo;
    private SocialNetworkService sns;

    @BeforeEach
    void setUp() {
        userRepo = Mockito.mock(UserRepo.class);
        friendshipRepo = Mockito.mock(FriendshipRepo.class);
        messageRepo = Mockito.mock(MessageRepo.class);
        UserValidator userValidator = Mockito.mock(UserValidator.class);
        sns = new SocialNetworkService(userRepo, friendshipRepo, messageRepo, userValidator);
    }

    @Test
    void addUser() {
        String username = "testUser";
        when(userRepo.findUserByUsername(username)).thenReturn(Optional.empty());
        when(userRepo.save(any(User.class)))
                .thenReturn(Optional.of(new User(
                        1L,
                        username,
                        "hashedPassword",
                        "/imag1.jpeg")));

        Optional<User> result = sns.addUser(username);

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getName());
        verify(userRepo, times(1)).findUserByUsername(username);
        verify(userRepo).save(any(User.class));
    }

    @Test
    void addUser_usernameAlreadyExists() {
        String username = "testUser";
        when(userRepo.findUserByUsername(username))
                .thenReturn(Optional.of(new User(1L, username, "pass" + username, "/imag1.jpeg")));

        Assertions.assertThrows(RuntimeException.class, () -> sns.addUser(username));
        verify(userRepo, never()).save(any());
    }

    @Test
    void addFriendship() {
        Long userId1 = 1L;
        Long userId2 = 2L;
        String status = "PENDING";
        
        ArgumentCaptor<Friendship> friendshipCaptor = ArgumentCaptor.forClass(Friendship.class);
        
        sns.addFriendship(userId1, userId2, status);
        
        verify(friendshipRepo).save(friendshipCaptor.capture());
        Friendship capturedFriendship = friendshipCaptor.getValue();
        
        assertEquals(userId1, capturedFriendship.getId().getLeft());
        assertEquals(userId2, capturedFriendship.getId().getRight());
        assertEquals(status, capturedFriendship.getStatus());
    }

    @Test
    void removeFriendship() {
        Long userId1 = 1L;
        Long userId2 = 2L;
        
        ArgumentCaptor<Tuple<Long, Long>> tupleCaptor = ArgumentCaptor.forClass(Tuple.class);
        
        sns.removeFriendship(userId1, userId2);
        
        verify(friendshipRepo).delete(tupleCaptor.capture());
        Tuple<Long, Long> capturedTuple = tupleCaptor.getValue();
        
        assertEquals(userId1, capturedTuple.getLeft());
        assertEquals(userId2, capturedTuple.getRight());
    }

    @Test
    void findNotFriendsByPrefix() {
        String prefix = "test";
        Long userId = 1L;
        List<User> expectedUsers = List.of(
            new User(2L, "testUser2", "pass", "/img.jpeg"),
            new User(3L, "testUser3", "pass", "/img.jpeg")
        );
        
        when(friendshipRepo.findNotFriendsByPrefix(prefix, userId)).thenReturn(expectedUsers);
        
        List<User> result = sns.findNotFriendsByPrefix(prefix, userId);
        
        assertEquals(expectedUsers, result);
        verify(friendshipRepo).findNotFriendsByPrefix(prefix, userId);
    }

    @Test
    void findUsersByPrefix() {
        String prefix = "test";
        Long userId = 1L;
        List<User> repoUsers = new ArrayList<>(List.of(
            new User(1L, "testUser1", "pass", "/img.jpeg"),
            new User(2L, "testUser2", "pass", "/img.jpeg"),
            new User(3L, "testUser3", "pass", "/img.jpeg")
        ));
        
        when(userRepo.findUsersByPrefix(prefix, userId)).thenReturn(repoUsers);
        
        List<User> result = sns.findUsersByPrefix(prefix, userId);
        
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(userId)));
        verify(userRepo).findUsersByPrefix(prefix, userId);
    }

    @Test
    void getUsers() {
        Long userId = 1L;
        List<User> allUsers = List.of(
            new User(1L, "testUser1", "pass", "/img.jpeg"),
            new User(2L, "testUser2", "pass", "/img.jpeg"),
            new User(3L, "testUser3", "pass", "/img.jpeg")
        );
        
        when(userRepo.findAll()).thenReturn(allUsers);
        
        List<User> result = sns.getUsers(userId);
        
        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(u -> u.getId().equals(userId)));
        verify(userRepo).findAll();
    }

    @Test
    void getNotFriends() {
        Long userId = 1L;
        List<User> expectedNotFriends = List.of(
            new User(2L, "testUser2", "pass", "/img.jpeg"),
            new User(3L, "testUser3", "pass", "/img.jpeg")
        );
        
        when(friendshipRepo.getNotFriendsRepository(userId)).thenReturn(expectedNotFriends);
        
        List<User> result = sns.getNotFriends(userId);
        
        assertEquals(expectedNotFriends, result);
        verify(friendshipRepo).getNotFriendsRepository(userId);
    }

    @Test
    void getPendingFriendships() {
        Long userId = 1L;
        Map<User, LocalDateTime> expectedPending = new HashMap<>();
        expectedPending.put(new User(2L, "testUser2", "pass", "/img.jpeg"), LocalDateTime.now());
        
        when(friendshipRepo.getPendingFriendships(userId)).thenReturn(expectedPending);
        
        Map<User, LocalDateTime> result = sns.getPendingFriendships(userId);
        
        assertEquals(expectedPending, result);
        verify(friendshipRepo).getPendingFriendships(userId);
    }

    @Test
    void findUser_exists() {
        Long userId = 1L;
        User expectedUser = new User(userId, "testUser", "pass", "/img.jpeg");
        
        when(userRepo.findOne(userId)).thenReturn(Optional.of(expectedUser));
        
        User result = sns.findUser(userId);
        
        assertEquals(expectedUser, result);
        verify(userRepo).findOne(userId);
    }

    @Test
    void findUser_notExists() {
        Long userId = 1L;
        
        when(userRepo.findOne(userId)).thenReturn(Optional.empty());
        
        User result = sns.findUser(userId);
        
        assertNull(result);
        verify(userRepo).findOne(userId);
    }

    @Test
    void updateFriendshipStatus() {
        Long userId1 = 1L;
        Long userId2 = 2L;
        String status = "ACCEPTED";
        
        ArgumentCaptor<Friendship> friendshipCaptor = ArgumentCaptor.forClass(Friendship.class);
        
        sns.updateFriendshipStatus(userId1, userId2, status);
        
        verify(friendshipRepo).update(friendshipCaptor.capture());
        Friendship capturedFriendship = friendshipCaptor.getValue();
        
        assertEquals(userId1, capturedFriendship.getId().getLeft());
        assertEquals(userId2, capturedFriendship.getId().getRight());
        assertEquals(status, capturedFriendship.getStatus());
    }

    @Test
    void sendMessage() {
        Long fromId = 1L;
        List<Long> toIds = Arrays.asList(2L, 3L);
        String messageContent = "Hello!";
        Long replyId = 5L;
        
        User fromUser = new User(fromId, "user1", "pass", "/img.jpeg");
        User toUser1 = new User(2L, "user2", "pass", "/img.jpeg");
        User toUser2 = new User(3L, "user3", "pass", "/img.jpeg");
        Message replyMessage = new Message(replyId, fromUser, List.of(toUser1), "Original", LocalDateTime.now(), null);
        
        when(userRepo.findOne(fromId)).thenReturn(Optional.of(fromUser));
        when(userRepo.findOne(2L)).thenReturn(Optional.of(toUser1));
        when(userRepo.findOne(3L)).thenReturn(Optional.of(toUser2));
        when(messageRepo.findOne(replyId)).thenReturn(Optional.of(replyMessage));
        
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        
        sns.sendMessage(fromId, toIds, messageContent, replyId);
        
        verify(messageRepo).save(messageCaptor.capture());
        Message capturedMessage = messageCaptor.getValue();
        
        assertEquals(fromUser, capturedMessage.getFrom());
        assertEquals(2, capturedMessage.getTo().size());
        assertTrue(capturedMessage.getTo().contains(toUser1));
        assertTrue(capturedMessage.getTo().contains(toUser2));
        assertEquals(messageContent, capturedMessage.getMessage());
        assertEquals(replyMessage, capturedMessage.getReply());
    }

    @Test
    void getChat() {
        Long fromId = 1L;
        Long toId = 2L;
        
        Message message1 = new Message(1L, null, null, "Hello", LocalDateTime.now().minusDays(1), null);
        Message message2 = new Message(2L, null, null, "Hi", LocalDateTime.now(), null);
        List<Message> unsortedMessages = Arrays.asList(message2, message1);
        
        when(messageRepo.findChat(fromId, toId)).thenReturn(unsortedMessages);
        
        List<Message> result = sns.getChat(fromId, toId);
        
        assertEquals(2, result.size());
        assertEquals(message1, result.get(0)); // Should be sorted by date
        assertEquals(message2, result.get(1));
        verify(messageRepo).findChat(fromId, toId);
    }

    @Test
    void findAllOnPage() {
        Long userId = 1L;
        Pageable pageable = mock(Pageable.class);
        Page<User> expectedPage = mock(Page.class);
        
        when(friendshipRepo.findFriendsOnPage(pageable, userId)).thenReturn(expectedPage);
        
        Page<User> result = sns.findAllOnPage(pageable, userId);
        
        assertEquals(expectedPage, result);
        verify(friendshipRepo).findFriendsOnPage(pageable, userId);
    }

    @Test
    void getNumberOfFriends() {
        Long userId = 1L;
        int expectedCount = 5;
        
        when(friendshipRepo.getNumberOfFriends(userId)).thenReturn(expectedCount);
        
        int result = sns.getNumberOfFriends(userId);
        
        assertEquals(expectedCount, result);
        verify(friendshipRepo).getNumberOfFriends(userId);
    }

    @Test
    void getTotalMessagesSent() {
        Long userId = 1L;
        int expectedCount = 10;
        
        when(messageRepo.getTotalMessagesSent(userId)).thenReturn(expectedCount);
        
        int result = sns.getTotalMessagesSent(userId);
        
        assertEquals(expectedCount, result);
        verify(messageRepo).getTotalMessagesSent(userId);
    }

    @Test
    void findUserByUsername_exists() {
        String username = "testUser";
        User expectedUser = new User(1L, username, "pass", "/img.jpeg");
        
        when(userRepo.findUserByUsername(username)).thenReturn(Optional.of(expectedUser));
        
        User result = sns.findUserByUsername(username);
        
        assertEquals(expectedUser, result);
        verify(userRepo).findUserByUsername(username);
    }

    @Test
    void findUserByUsername_notExists() {
        String username = "testUser";
        
        when(userRepo.findUserByUsername(username)).thenReturn(Optional.empty());
        
        User result = sns.findUserByUsername(username);
        
        assertNull(result);
        verify(userRepo).findUserByUsername(username);
    }
}