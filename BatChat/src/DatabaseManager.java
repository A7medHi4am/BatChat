//package com.mycompany.batchat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/batchat";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private List<User> users;


    public DatabaseManager() {
        users = new ArrayList<>();
    }

    private Connection connection;

    // Connect to the database
    public void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
        }
    }

    // Check login credentials
    public boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // Returns true if a match is found
        } catch (SQLException e) {
            System.err.println("Error validating login: " + e.getMessage());
            return false;
        }
    }

    // Close the database connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing the database connection: " + e.getMessage());
        }
    }

    // Get the user ID based on username
    public int getUserIdByUsername(String username) {
        String query = "SELECT userID FROM user WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("userID"); // Use userID instead of id
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user ID: " + e.getMessage());
        }
        return -1; // Return -1 if user not found
    }

    // Register a new user
    public boolean registerUser(String fullName, String username, String password) {
        String checkQuery = "SELECT COUNT(*) FROM user WHERE username = ?";
        String insertQuery = "INSERT INTO user (full_name, username, password, status) VALUES (?, ?, ?, 'offline')";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {

            // Check if the username already exists
            checkStmt.setString(1, username);
            ResultSet resultSet = checkStmt.executeQuery();
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return false; // Username exists
            }

            // Insert the new user
            insertStmt.setString(1, fullName);
            insertStmt.setString(2, username);
            insertStmt.setString(3, password);
            insertStmt.executeUpdate();
            return true; // Account created successfully
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    // Fetch online users
    public List<User> getOnlineUsers(String currentUser) {
        List<User> onlineUsers = new ArrayList<>();
        String query = "SELECT username, password, status FROM user WHERE status = 'online' AND username != ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentUser);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                String username = rs.getString("username");
                String password = rs.getString("password");
                String status = rs.getString("status");


                User user = new User(username, password, status);
                onlineUsers.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching online users: " + e.getMessage());
        }
        return onlineUsers;
    }

    // DatabaseManager method for retrieving chat history
    public List<Message> getChatHistory(String senderUsername, String receiverUsername) {
        List<Message> chatHistory = new ArrayList<>();
        int senderId = getUserIdByUsername(senderUsername);
        int receiverId = getUserIdByUsername(receiverUsername);

        if (senderId == -1 || receiverId == -1) {
            System.err.println("Error: Invalid sender or receiver username.");
            return chatHistory;
        }

        String query = "SELECT senderID, content, timestamp FROM message WHERE " +
                "(senderID = ? AND receiverID = ?) OR (senderID = ? AND receiverID = ?) ORDER BY timestamp";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, receiverId);
            stmt.setInt(4, senderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int sender = rs.getInt("senderID");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");

                String senderName = (sender == senderId) ? senderUsername : receiverUsername;

                // Use the unified Message class

                chatHistory.add(new Message(senderName, content, timestamp));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching chat history: " + e.getMessage());
        }

        return chatHistory;
    }
    // Save a message to the database
    public int saveMessage(String senderUsername, String receiverUsername, String message) {
        int senderId = getUserIdByUsername(senderUsername);
        int receiverId = getUserIdByUsername(receiverUsername);

        if (senderId == -1 || receiverId == -1) {
            System.err.println("Error: Invalid sender or receiver username.");
            return -1; // Indicate failure
        }

        String query = "INSERT INTO message (senderID, receiverID, content) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, message);
            stmt.executeUpdate();

            // Retrieve the generated messageID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return the messageID
            }
        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
        return -1;
    }


    // Create a group chat
    public boolean createGroupChat(String roomName, String createdByUsername) {
        int createdBy = getUserIdByUsername(createdByUsername);

        if (createdBy == -1) {
            System.err.println("Error: Invalid username for group creator.");
            return false;
        }

        String query = "INSERT INTO GroupChat (RoomName, CreatedBy) VALUES (?, ?)";
        try {
            connection.setAutoCommit(false); // Disable autocommit

            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, roomName);
                stmt.setInt(2, createdBy);
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int chatRoomID = generatedKeys.getInt(1);
                        System.out.println("Group chat created with ID: " + chatRoomID);

                        // Add the creator as a participant
                        addParticipantToGroupChat(roomName, createdByUsername);

                        connection.commit(); // Commit the transaction
                        return true;
                    } else {
                        connection.rollback(); // Rollback the transaction
                        throw new SQLException("Creating group chat failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback the transaction in case of error
            } catch (SQLException rollbackException) {
                System.err.println("Error rolling back transaction: " + rollbackException.getMessage());
            }
            System.err.println("Error creating group chat: " + e.getMessage());
            return false;
        } finally {
            try {
                connection.setAutoCommit(true); // Enable autocommit
            } catch (SQLException e) {
                System.err.println("Error setting autocommit: " + e.getMessage());
            }
        }
    }
    // Add a participant to a group chat
    public boolean addParticipantToGroupChat(String roomName, String participantUsername) {
        int userId = getUserIdByUsername(participantUsername);
        int chatRoomId = getChatRoomIdByRoomName(roomName);

        if (userId == -1 || chatRoomId == -1) {
            System.err.println("Error: Invalid group or participant.");
            return false;
        }

        String query = "INSERT INTO GroupChatParticipant (ChatRoomID, UserID) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chatRoomId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding participant to group chat: " + e.getMessage());
            return false;
        }
    }
    public List<User> getGroupParticipants(String roomName) {
        List<User> participants = new ArrayList<>();
        int chatRoomId = getChatRoomIdByRoomName(roomName);

        if (chatRoomId == -1) {
            System.err.println("Error: Invalid group name.");
            return participants;
        }

        String query = "SELECT u.username, u.password, u.status FROM User u " +
                "JOIN GroupChatParticipant gcp ON u.userID = gcp.UserID " +
                "WHERE gcp.ChatRoomID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chatRoomId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String status = rs.getString("status");
                participants.add(new User(username, password, status));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching group participants: " + e.getMessage());
        }
        return participants;
    }
    // Get all group chats the user is a participant in
    public List<GroupChat> getUserGroups(String username) {
        List<GroupChat> groups = new ArrayList<>();
        String query = "SELECT gc.RoomName FROM GroupChat gc "
                + " INNER JOIN GroupChatParticipant gcp"
                + " ON gc.ChatRoomID = gcp.ChatRoomID"
                + " INNER JOIN User u ON gcp.UserID = u.userID"
                + " WHERE u.username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username); // Set the username for the query
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String roomName = rs.getString("RoomName");
                    groups.add(new GroupChat(roomName)); // Add the group object
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user group chats: " + e.getMessage());
        }

        return groups;
    }

    // Get all group chat names
    public List<GroupChat> getAllGroups() {
        List<GroupChat> groups = new ArrayList<>();
        String query = "SELECT RoomName FROM GroupChat";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String roomName = rs.getString("RoomName");
                groups.add(new GroupChat(roomName)); // Add the group object instead of just the name
            }

        } catch (SQLException e) {
            System.err.println("Error fetching group chat names: " + e.getMessage());
        }

        return groups;
    }

    // Save a message in a group chat
    public void saveGroupMessage(String roomName, String senderUsername, String message) {
        int senderId = getUserIdByUsername(senderUsername);
        int chatRoomId = getChatRoomIdByRoomName(roomName);

        if (senderId == -1 || chatRoomId == -1) {
            System.err.println("Error: Invalid sender or group chat.");
            return;
        }

        String query = "INSERT INTO message (senderID, receiverID, content) VALUES (?, NULL, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, senderId);
            stmt.setString(2, message);
            stmt.executeUpdate();
            System.out.println("Message saved successfully.");
        } catch (SQLException e) {
            System.err.println("Error saving group chat message: " + e.getMessage());
        }
    }

    public List<Message> getGroupChatMessages(String roomName) {
        List<Message> messages = new ArrayList<>();
        int chatRoomId = getChatRoomIdByRoomName(roomName);

        if (chatRoomId == -1) {
            System.err.println("Error: Invalid group name.");
            return messages;
        }

        String query = "SELECT u.username, m.content, m.timestamp " +
                "FROM Message m " +
                "JOIN GroupChatParticipant gcp ON gcp.UserID = m.senderID " +
                "JOIN User u ON u.userID = m.senderID " +
                "WHERE gcp.ChatRoomID = ? AND m.receiverID IS NULL " +
                "ORDER BY m.timestamp";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chatRoomId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String senderName = rs.getString("username");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");

                Message message = new Message(senderName, content, timestamp);
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching group chat messages: " + e.getMessage());
        }
        return messages;
    }
    public List<Message> getSavedMessagesForGroup(String roomName) {
        return getGroupChatMessages(roomName);
    }


    // Helper method to get ChatRoomID by RoomName
    private int getChatRoomIdByRoomName(String roomName) {
        String query = "SELECT ChatRoomID FROM GroupChat WHERE RoomName = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, roomName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ChatRoomID");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ChatRoomID: " + e.getMessage());
        }
        return -1; // Return -1 if not found
    }

    public void saveAttachment(int messageID, String filePath, String fileType, int fileSize) {
        String query = "INSERT INTO Attachment (messageID, filePath, fileType, size) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, messageID);
            stmt.setString(2, filePath);
            stmt.setString(3, fileType);
            stmt.setInt(4, fileSize);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving attachment: " + e.getMessage());
        }
    }

    public int saveGroupMessageWithAttachment(String roomName, String senderUsername, String message, String filePath) {
        int senderId = getUserIdByUsername(senderUsername);  // Get sender's ID
        int chatRoomId = getChatRoomIdByRoomName(roomName);   // Get chat room ID

        if (senderId == -1 || chatRoomId == -1) {
            System.err.println("Error: Invalid sender or group chat.");
            return -1;  // Return -1 to indicate failure
        }

        // Save the message into the Message table
        String query = "INSERT INTO Message (senderID, receiverID, content) VALUES (?, NULL, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, senderId);
            stmt.setString(2, message);  // Message content
            stmt.executeUpdate();

            // Retrieve the generated messageID
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);  // Return the generated message ID
            }
        } catch (SQLException e) {
            System.err.println("Error saving group chat message with attachment: " + e.getMessage());
        }
        return -1;  // Return -1 in case of failure
    }



    public List<Attachment> getAttachmentsForMessage(int messageID) {
        List<Attachment> attachments = new ArrayList<>();
        String query = "SELECT * FROM Attachment WHERE messageID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, messageID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Attachment attachment = new Attachment(
                        rs.getInt("attachmentID"),
                        rs.getInt("messageID"),
                        rs.getString("filePath"),
                        rs.getString("fileType"),
                        rs.getInt("size")
                );
                attachments.add(attachment);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attachments: " + e.getMessage());
        }
        return attachments;
    }

    // Check if a username already exists in the database
    public boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM user WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Insert a new user into the database
    public boolean createUser(User user) {
        String query = "INSERT INTO user (username, password, full_name) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;  // Returns true if user was successfully created
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
