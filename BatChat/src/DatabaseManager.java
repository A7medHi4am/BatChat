import java.sql.*;
import java.util.ArrayList;
import java.util.List;


class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/batchat";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";
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
        String query = "INSERT INTO user (username, password, first_name, last_name) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;  // Returns true if user was successfully created
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Create a private chat between two users
    public boolean createPrivateChat(String senderUsername, String recipientUsername) {
        int senderId = getUserIdByUsername(senderUsername);
        int recipientId = getUserIdByUsername(recipientUsername);

        if (senderId == -1 || recipientId == -1) {
            System.err.println("Error: Invalid sender or recipient username.");
            return false;
        }

        // Check if a private chat already exists between these two users
        String checkQuery = "SELECT chatID FROM PrivateChat WHERE (user1ID = ? AND user2ID = ?) OR (user1ID = ? AND user2ID = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, recipientId);
            stmt.setInt(3, recipientId);
            stmt.setInt(4, senderId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Private chat already exists between " + senderUsername + " and " + recipientUsername);
                return false;  // A chat already exists
            }
        } catch (SQLException e) {
            System.err.println("Error checking for existing private chat: " + e.getMessage());
        }

        // Create a new private chat
        String query = "INSERT INTO PrivateChat (user1ID, user2ID) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, recipientId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating private chat: " + e.getMessage());
        }
        return false;
    }

    // Fetch users with whom the current user has a private chat
    public List<User> getChatContacts(String currentUser) {
        List<User> chatContacts = new ArrayList<>();
        int currentUserId = getUserIdByUsername(currentUser);

        if (currentUserId == -1) {
            System.err.println("Error: Invalid current user.");
            return chatContacts;
        }

        String query = "SELECT DISTINCT u.userID, u.username, u.password, u.first_name, u.last_name " +
                "FROM user u " +
                "INNER JOIN PrivateChat pc ON (pc.user1ID = u.userID OR pc.user2ID = u.userID) " +
                "WHERE (pc.user1ID = ? OR pc.user2ID = ?) AND u.userID != ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, currentUserId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");

                User user = new User(username, password, firstName, lastName);
                chatContacts.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching chat contacts: " + e.getMessage());
        }

        return chatContacts;
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


    // Create a group chat and add the creator as a participant
    public boolean createGroupChat(String roomName, String createdByUsername) {
        int createdBy = getUserIdByUsername(createdByUsername);

        if (createdBy == -1) {
            System.err.println("Error: Invalid username for group creator.");
            return false;
        }

        String query = "INSERT INTO GroupChat (RoomName, CreatedBy) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, roomName);
            stmt.setInt(2, createdBy);
            stmt.executeUpdate();

            // Retrieve the generated ChatRoomID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int chatRoomId = rs.getInt(1);

                // Add the creator as a participant
                String participantQuery = "INSERT INTO GroupChatParticipant (ChatRoomID, UserID) VALUES (?, ?)";
                try (PreparedStatement participantStmt = connection.prepareStatement(participantQuery)) {
                    participantStmt.setInt(1, chatRoomId);
                    participantStmt.setInt(2, createdBy);
                    participantStmt.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating group chat: " + e.getMessage());
        }
        return false;
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


    // Save a message in a group chat
    public void saveGroupMessage(String roomName, String senderUsername, String message) {
        int senderId = getUserIdByUsername(senderUsername);
        int chatRoomId = getChatRoomIdByRoomName(roomName);

        if (senderId == -1 || chatRoomId == -1) {
            System.err.println("Error: Invalid sender or group chat.");
            return;
        }

        String query = "INSERT INTO Message (senderID, receiverID, content, ChatRoomID) VALUES (?, NULL, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, senderId);
            stmt.setString(2, message);
            stmt.setInt(3, chatRoomId);
            stmt.executeUpdate();
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
                "JOIN User u ON u.userID = m.senderID " +
                "WHERE m.ChatRoomID = ? " +
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

        // Save the message into the Message table, including the ChatRoomID
        String query = "INSERT INTO Message (senderID, receiverID, content, ChatRoomID) VALUES (?, NULL, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, senderId);
            stmt.setString(2, message);  // Message content
            stmt.setInt(3, chatRoomId);  // Include ChatRoomID
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
}

