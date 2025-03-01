package com.example.social_network.repository;

import com.example.social_network.domain.Constants;
import com.example.social_network.domain.Friendship;
import com.example.social_network.domain.Tuple;
import com.example.social_network.domain.User;
import com.example.social_network.paging.Page;
import com.example.social_network.paging.Pageable;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FriendshipRepo implements Repository<Tuple<Long, Long>, Friendship> {
   private static final Logger logger = LoggerFactory.getLogger(FriendshipRepo.class);

    private final String url;
    private final String user;
    private final String password;
    private final String photosFolder;

    public FriendshipRepo(
                            String url,
                            String user,
                            String password,
                            String photosFolder) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.photosFolder = photosFolder;
    }

    public Optional<Friendship> findOne(Tuple<Long, Long> ID) {
        String query = "SELECT * FROM Friendships WHERE (ID1 = ? AND ID2 = ?) OR (ID1 = ? AND ID2 = ?)";
        Friendship friendship = null;
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, ID.getLeft());
            statement.setLong(2, ID.getRight());
            statement.setLong(3, ID.getRight());
            statement.setLong(4, ID.getLeft());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Long idFriend1 = resultSet.getLong("ID1");
                Long idFriend2 = resultSet.getLong("ID2");
                Timestamp date = resultSet.getTimestamp("F_DATE");
                LocalDateTime localDateTime = date.toLocalDateTime();
                String status = resultSet.getString("STATUS");
                friendship = new Friendship(idFriend1, idFriend2, localDateTime, status);
                friendship.setId(new Tuple<>(idFriend1, idFriend2));
            }

        } catch (SQLException e) {
            logger.error("Error getting friendship {}: {}", ID, e.getMessage());
        }
        return Optional.ofNullable(friendship);
    }

    @Override
    public Iterable<Friendship> findAll() {
        Map<Tuple<Long, Long>, Friendship> friendships = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM Friendships");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long idFriend1 = resultSet.getLong("ID1");
                Long idFriend2 = resultSet.getLong("ID2");
                Timestamp date = resultSet.getTimestamp("F_DATE");
                LocalDateTime localDateTime = date.toLocalDateTime();
                String status = resultSet.getString("STATUS");

                Friendship friendship = new Friendship(idFriend1, idFriend2, localDateTime, status);
                friendship.setId(new Tuple<>(idFriend1, idFriend2));
                friendships.put(friendship.getId(), friendship);
            }

        } catch (SQLException e) {
            logger.error("Error getting friendships: {}", e.getMessage());
        }
        return friendships.values();
    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        String query = "INSERT INTO Friendships(ID1, ID2, F_DATE, STATUS) VALUES (?,?,?,?)";
        Friendship friendship = null;

        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, entity.getId().getLeft());
            statement.setLong(2, entity.getId().getRight());
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(entity.getDate()));
            statement.setString(4, entity.getStatus());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                friendship = entity;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.ofNullable(friendship);
    }

    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> ID) {
        Optional<Friendship> friendshipToDelete = Optional.empty();

        String query = "DELETE FROM Friendships WHERE ID1 = ? AND ID2 = ? OR ID1 = ? AND ID2 = ?";
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, ID.getLeft());
            statement.setLong(2, ID.getRight());
            statement.setLong(3, ID.getRight());
            statement.setLong(4, ID.getLeft());

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                friendshipToDelete = findOne(ID);
            }
        } catch (SQLException e) {
            logger.error("Error deleting friendship: {}", e.getMessage());
        }

        return friendshipToDelete;
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        Friendship friendship = null;
        String query = "UPDATE Friendships SET F_DATE = ?, STATUS = ? WHERE ID1 = ? AND ID2 = ? OR ID1 = ? AND ID2 = ?";

        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, java.sql.Timestamp.valueOf(entity.getDate()));
            statement.setString(2, entity.getStatus());
            statement.setLong(3, entity.getId().getLeft());
            statement.setLong(4, entity.getId().getRight());
            statement.setLong(5, entity.getId().getRight());
            statement.setLong(6, entity.getId().getLeft());
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                friendship = entity;
            }
        } catch (SQLException e) {
            logger.error("Database error while updating friendship: {}", e.getMessage(), e);
        }

        return Optional.ofNullable(friendship);
    }

    public List<User> findNotFriendsByPrefix(String prefix, Long userId) {
        String sql = "SELECT * FROM users WHERE name LIKE ? AND id NOT IN (" +
                "SELECT id1 FROM friendships WHERE id2 = ? " +
                "UNION " +
                "SELECT id2 FROM friendships WHERE id1 = ?)" +
                "AND id <> ?";
        List<User> users = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, this.user, password);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, prefix + "%");
            preparedStatement.setLong(2, userId);
            preparedStatement.setLong(3, userId);
            preparedStatement.setLong(4, userId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                String password = rs.getString("password");
                String profileImagePath = rs.getString("profile_image_path");
                users.add(new User(id, name, password, profileImagePath));
            }
        } catch (SQLException e) {
            logger.error("Database error: {}", e.getMessage());
        }
        return users;
    }

    public List<User> getNotFriendsRepository(Long userId) {
        String sql = "SELECT * FROM users WHERE id NOT IN " +
                "(SELECT id1 FROM friendships WHERE id2 = ? " +
                "UNION SELECT id2 FROM friendships WHERE id1 = ?)" +
                "AND id <> ?";
        List<User> notFriends = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, this.user, password);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, userId);
            preparedStatement.setLong(3, userId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                String password = rs.getString("password");
                String profileImagePath = rs.getString("profile_image_path");
                notFriends.add(new User(id, name, password, profileImagePath));
            }
        } catch (SQLException e) {
            logger.error("Database error while getNotFriendsRepository: {}", e.getMessage());
        }
        return notFriends;
    }

    public Map<User, LocalDateTime> getPendingFriendships(Long userId) {
        String sql = "SELECT u.id, u.name, u.profile_image_path, f.f_date FROM users u JOIN friendships f ON (u.id = f.id1 OR u.id = f.id2) WHERE (f.id1 = ? OR f.id2 = ?) AND f.status = ? AND u.id <> ?";
        Map<User, LocalDateTime> pendingFriendships = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(url, this.user, password);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            String pendingStatus = Constants.PENDING + userId;
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, userId);
            preparedStatement.setString(3, pendingStatus);
            preparedStatement.setLong(4, userId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                String profileImagePath = rs.getString("profile_image_path");
                LocalDateTime date = rs.getTimestamp("f_date").toLocalDateTime();
                pendingFriendships.put(new User(id, name, "no password needed", profileImagePath), date);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return pendingFriendships;
    }

    public Page<User> findFriendsOnPage(Pageable pageable, Long id1) {
        List<User> friendsOnPage = new ArrayList<>();
        String sql = "SELECT u.id, u.name, u.profile_image_path FROM users u JOIN friendships f ON (u.id = f.id1 OR u.id = f.id2) WHERE (f.id1 = ? OR f.id2 = ?) AND f.status = 'active' AND u.id <> ?";
        sql += " limit ? offset ?";
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id1);
            statement.setLong(2, id1);
            statement.setLong(3, id1); // să nu fie afișat ca prieten al său
            statement.setInt(4, pageable.getPageSize());
            statement.setInt(5, pageable.getPageSize() * pageable.getPageNumber());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String profileImagePath = resultSet.getString("profile_image_path");
                friendsOnPage.add(new User(id, name, "no password needed",photosFolder + profileImagePath));
            }
        } catch (SQLException e) {
            logger.error("Database error: {}", e.getMessage());
        }
        return new Page<>(friendsOnPage, getNumberOfFriends(id1));
    }

    public int getNumberOfFriends(Long id) {
        int numberOfFriends = 0;
        String sql = "SELECT COUNT(*) AS count FROM friendships WHERE (id1 = ? OR id2 = ?) AND status = 'active'";
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setLong(2, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                numberOfFriends = resultSet.getInt("count");
            }
        } catch (SQLException e) {
            logger.error("Database error: {}", e.getMessage());
        }
        return numberOfFriends;
    }
}
