import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// User Class
class User {
    private int userID;
    private String username;
    private String password;
    private String status;

    public User(int userID, String username, String password, String status) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.status = status;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void updateStatus(String status) {
        this.status = status;
    }
}

// Message Class
class Message {
    private int messageID;
    private int senderID;
    private int receiverID;
    private String content;
    private LocalDateTime timestamp;

    public Message(int messageID, int senderID, int receiverID, String content, LocalDateTime timestamp) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.content = content;
        this.timestamp = timestamp;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public int getSenderID() {
        return senderID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String formatMessage() {
        return "[" + timestamp + "] " + senderID + ": " + content;
    }
}

// Base Chat Class
abstract class Chat {
    private int chatID;

    public Chat(int chatID) {
        this.chatID = chatID;
    }

    public int getChatID() {
        return chatID;
    }

    public void setChatID(int chatID) {
        this.chatID = chatID;
    }

    public abstract void sendMessage(Message message);
}

// PrivateChat Class
class PrivateChat extends Chat {
    private User user1;
    private User user2;

    public PrivateChat(int chatID, User user1, User user2) {
        super(chatID);
        this.user1 = user1;
        this.user2 = user2;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    @Override
    public void sendMessage(Message message) {
        System.out.println("Message in PrivateChat between " + user1.getUsername() + " and " + user2.getUsername() +
                ": " + message.getContent());
    }
}

// GroupChat Class
class GroupChat extends Chat {
    private String roomName;
    private List<User> participants;
    private List<Message> messages;

    public GroupChat(int chatID, String roomName) {
        super(chatID);
        this.roomName = roomName;
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void addParticipant(User user) {
        participants.add(user);
    }

    public void removeParticipant(User user) {
        participants.remove(user);
    }

    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public void sendMessage(Message message) {
        messages.add(message);
        System.out.println("Message in GroupChat '" + roomName + "' by User ID " + message.getSenderID() +
                ": " + message.getContent());
    }
}

// Attachment Class
class Attachment {
    private int attachmentID;
    private int messageID;
    private String filePath;
    private String fileType;
    private double size;

    public Attachment(int attachmentID, int messageID, String filePath, String fileType, double size) {
        this.attachmentID = attachmentID;
        this.messageID = messageID;
        this.filePath = filePath;
        this.fileType = fileType;
        this.size = size;
    }

    public int getAttachmentID() {
        return attachmentID;
    }

    public void setAttachmentID(int attachmentID) {
        this.attachmentID = attachmentID;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void downloadAttachment() {
        System.out.println("Downloading attachment from: " + filePath);
    }
}
