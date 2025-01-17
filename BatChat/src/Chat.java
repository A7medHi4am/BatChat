import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;


// User Class
class User {
    private int userID;
    private String username;
    private String password;
    private String status;
    private String firstName;
    private String lastName;

    public User(int userID, String username, String password, String status, String firstName, String lastName) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.status = status;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(String username, String password, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}



// Message Class
class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private int messageID;
    private int senderID;
    private String sender;
    private int receiverID;
    private String receiver;
    private String content;
    private String timestamp;

    public Message(int messageID, int senderID, String sender, int receiverID, String receiver, String content, String timestamp) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.sender = sender;
        this.receiverID = receiverID;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;

    }

    public Message(String senderName, String content, String timestamp) {
        this.sender = senderName;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}

// Base Chat Class
abstract class Chat {
    private int chatID;

    public Chat(int chatID) {
        this.chatID = chatID;
    }

    public Chat() {

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

    public GroupChat(int chatID, List<Message> messages) {
        super(chatID);
        this.messages = messages;
    }

    public GroupChat(String roomName) {
        super();
        this.roomName=roomName;
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
    public void addMessage(Message message) {
        this.messages.add(message);
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
