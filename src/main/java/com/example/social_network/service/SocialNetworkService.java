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
import com.example.social_network.util.PasswordUtil;
import com.example.social_network.validator.UserValidator;

import java.util.*;
import java.time.LocalDateTime;

public class SocialNetworkService {
    private final UserRepo userRepo;
    private final FriendshipRepo friendshipRepo;
    private final MessageRepo messageRepo;
    private final UserValidator userValidator;

    public SocialNetworkService(
            UserRepo userRepo,
            FriendshipRepo friendshipRepo,
            MessageRepo messageRepo,
            UserValidator userValidator) {
        this.userRepo  = userRepo;
        this.friendshipRepo = friendshipRepo;
        this.messageRepo = messageRepo;
        this.userValidator = userValidator;
    }

    public Optional<User> addUser(String username) {
        userValidator.validate(new User(null, username, null, null));
        if (userRepo.findUserByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        String plainPassword = "pass" + username;
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        int imageNumber = new Random().nextInt(6) + 1;
        String photo =  "/imag" + imageNumber + ".jpeg";
        User user = new User(null, username, hashedPassword, photo);
        return userRepo.save(user);
    }

    public Optional<Friendship> addFriendship(Long userId1, Long userId2, String Status) {
        Friendship friendship = new Friendship(userId1, userId2, LocalDateTime.now(), Status);
        return friendshipRepo.save(friendship);
    }

    public Optional<Friendship> removeFriendship(Long userId1, Long userId2) {
        // remove friendship having userId1 and userId2
        // return the removed friendship
        // or null if there is no friendship with the given ids
        return friendshipRepo.delete(new Tuple<>(userId1, userId2));

    }

    public void updateFriendshipStatus(Long idFriend1, Long idFriend2, String active) {
        // update friendship having userId1 and userId2
        friendshipRepo.update(new Friendship(idFriend1, idFriend2, LocalDateTime.now(), active));
    }

    public List<User> findNotFriendsByPrefix(String prefix, Long userID) {
        return friendshipRepo.findNotFriendsByPrefix(prefix, userID);
    }

    public List<User> findUsersByPrefix(String prefix, Long userID) {
        List<User> users = new ArrayList<>();
        userRepo.findUsersByPrefix(prefix, userID).forEach(users::add);
        return users;
    }

   public List<User> getUsers(Long userId) {
        List<User> users = new ArrayList<>();
        userRepo.findAll().forEach(users::add);
        users.removeIf(user -> user.getId().equals(userId));
        return users;
}

    public List<User> getNotFriends(Long userId) {
        return friendshipRepo.getNotFriendsRepository(userId);
    }

    public Map<User, LocalDateTime> getPendingFriendships(Long userId) {
        return friendshipRepo.getPendingFriendships(userId);
    }

    public User findUser(Long userId) {
        return userRepo.findOne(userId).orElse(null);
    }

    public Optional<Message> sendMessage(Long from, List<Long> to, String message, Long reply) {
        User userFrom = userRepo.findOne(from).orElse(null);
        List<User> usersTo = new ArrayList<>();
        to.forEach(id -> usersTo.add(userRepo.findOne(id).orElse(null)));
        Message replyMessage = messageRepo.findOne(reply).orElse(null);

        Message newMessage = new Message(null, userFrom, usersTo, message,
                LocalDateTime.now().withNano(0), replyMessage);

        return messageRepo.save(newMessage);
    }

    public List<Message> getChat(Long from, Long to) {
        // Get chat sorted by date - oldest first
        List<Message> messages = (List<Message>) messageRepo.findChat(from, to);
        messages.sort(Comparator.comparing(Message::getDate));
        return messages;
    }

    public Page<User> findAllOnPage(Pageable pageable, Long id)  {
        return friendshipRepo.findFriendsOnPage(pageable, id);
    }

    public int getNumberOfFriends(Long id) {
        return friendshipRepo.getNumberOfFriends(id);
    }

    public int getTotalMessagesSent(Long id) {
        return messageRepo.getTotalMessagesSent(id);
    }

    public User findUserByUsername(String username) {
        return userRepo.findUserByUsername(username).orElse(null);
    }
}
