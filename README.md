# Social_Network

This project is a Java application for a Facebook-style social network, running with a JavaFX graphical interface and PostgreSQL database. The purpose of the application is to allow users to register, log in, add friends, send private messages, and view chats.

## Main Features

- **User authentication:** Uses encrypted passwords with BCrypt.
- **User account management:** Create, delete, and update accounts.
- **Friendship management:** Add, accept, reject, and delete friendships.
- **Messaging:** Send and receive messages (including replying to previous messages).
- **Friends list:** View and paginate your friends list within the graphical interface.
- **Notifications:** Get notified about pending friendship requests.

## System Requirements

- **Java:** Version 17.
- **JavaFX:** [Download and configuration instructions](https://openjfx.io/).
- **PostgreSQL:** Ensure it is installed and properly configured.

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

## Database Configuration Scripts

To configure the database required for the application, follow these steps in your terminal:

```sql
-- Connect to PostgreSQL
psql postgres

-- Create database
CREATE DATABASE social_network;

-- Connect to the database
\connect social_network;

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

CREATE TABLE Friendships (
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

-- Verify tables
\dt
