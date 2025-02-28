# Social_Network

This project is a Java application for a Facebook-style social network, running with a JavaFX graphical interface and PostgreSQL database. The purpose of the application is to allow users to register, interact, and communicate with each other.

## Main Features

- **User authentication:** Uses encrypted passwords with BCrypt.
- **User account management:** Create, delete, and update accounts.
- **Friendship management:** Add, accept, reject, and delete friendships.
- **Messaging:** Send and receive messages (including replying to previous messages).
- **Friends list:** View and paginate your friends list within the graphical interface.
- **Notifications:** Get notified about pending friendship requests.

## System Requirements

- **Java:** Version 17.
- **JavaFX:** Version 21.0.6.
- **PostgreSQL:** Ensure it is installed and properly configured.

## Installation and Configuration

### Java 17

1. Download and install Java 17 from the [Oracle website](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html).
2. Verify the installation by running:
    ```sh
    java -version
    ```
   Ensure it displays Java 17.

### JavaFX

1. Download JavaFX from [Gluon](https://gluonhq.com/products/javafx/).
2. Follow the installation instructions for your operating system.
3. Add the JavaFX libraries to your project.

### PostgreSQL

1. Download and install PostgreSQL from the [official website](https://www.postgresql.org/download/).
2. During installation, set up a password for the `postgres` user.
3. Create a new database for the application:
    ```sh
    psql postgres
    CREATE DATABASE social_network;
    \connect social_network;
    ```

### Database Configuration

Create the necessary tables in PostgreSQL:

```sql
-- Create tables
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    profile_image_path VARCHAR(255)
);

CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    from_user_id INTEGER NOT NULL,
    message TEXT NOT NULL,
    date TIMESTAMP NOT NULL,
    reply_to INTEGER,
    CONSTRAINT fk_from_user
        FOREIGN KEY (from_user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reply_message
        FOREIGN KEY (reply_to)
        REFERENCES messages(id)
        ON DELETE SET NULL
);

CREATE TABLE message_recipients (
    message_id INTEGER NOT NULL,
    to_user_id INTEGER NOT NULL,
    PRIMARY KEY (message_id, to_user_id),
    CONSTRAINT fk_message_id
        FOREIGN KEY (message_id)
        REFERENCES messages(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_to_user
        FOREIGN KEY (to_user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE friendships (
    ID1 INTEGER NOT NULL,
    ID2 INTEGER NOT NULL,
    F_DATE TIMESTAMP NOT NULL,
    STATUS VARCHAR(50) NOT NULL,
    PRIMARY KEY (ID1, ID2),
    CONSTRAINT fk_friendship_user1
        FOREIGN KEY (ID1)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_friendship_user2
        FOREIGN KEY (ID2)
        REFERENCES users(id)
        ON DELETE CASCADE
);
```

### Project Configuration

1. Ensure that `HelloApplication.java` and other classes are correctly set up to connect to the PostgreSQL database. The main configuration file is located in the path:
    ```
    src/main/java/com/example/social_network/HelloApplication.java
    ```

2. Update the database connection details in `DBConnectionAndProfileImagesPath.java`:
    ```java
    private static final String URL = "jdbc:postgresql://localhost:5432/social_network";
    private static final String USER = "postgres";
    private static final String PASSWORD = "your_password";
    ```

## Project Structure

The main Java files are in the `com.example.social_network` package:

- **HelloApplication.java** – The starter class for the JavaFX application.
- **MainController.java** – Manages the main interface (listing friends, search functionality, etc.).
- **LoginController.java** – Handles the login screen.
- **ChatController.java** – Manages the chat window (displaying messages, sending messages, replying, etc.).
- **MultipleMessage.java** – Facilitates sending messages to multiple recipients.
- **DBConnectionAndProfileImagesPath.java** – A singleton for database connection configuration.

### Service Package

- **SocialNetworkService.java** – Contains the core application logic (friendships, messaging, data display, etc.).

### Repository Package

- **dbUserRepo.java** – Handles interactions with the `users` table.
- **dbFriendshipRepo.java** – Handles interactions with the `friendships` table.
- **MessageRepo.java** – Manages interactions with the `messages` table and its relationship with message recipients.

### Domain Package (Model)

Contains the model classes:

- **User.java, Friendship.java, Message.java, Tuple.java, etc.**
- These classes represent the database entities and the data managed by the application.

### Utility Package

- **PasswordUtil.java** – Provides password encryption and verification functionalities using BCrypt.

### JavaFX Resources (Scene Builder)

- **FXML Files:** For screens like `login-view.fxml`, `main-view.fxml`, `chat-view.fxml`, etc.
- **CSS/Style Files:** Such as `style.css`.

## Database

The following tables need to exist in PostgreSQL:

### users
- **Columns:** `ID` (SERIAL PRIMARY KEY), `name` (VARCHAR), `password` (VARCHAR), `profile_image_path` (VARCHAR).

### friendships
- **Columns:** `ID1` (INTEGER), `ID2` (INTEGER), `F_DATE` (TIMESTAMP), `STATUS` (VARCHAR).  
  *Note:* `ID1` and `ID2` represent the IDs of users who form a friendship.

### messages
- **Columns:** `id` (SERIAL PRIMARY KEY), `from_user_id` (INTEGER), `message` (TEXT), `date` (TIMESTAMP), `reply_to` (INTEGER).

### message_recipients
- **Columns:** `message_id` (INTEGER), `to_user_id` (INTEGER).  
  This table maps messages to their respective recipients.
