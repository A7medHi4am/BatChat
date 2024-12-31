//package com.mycompany.batchat;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.nio.file.StandardCopyOption;

public class App extends Application {
    private String currentUser;

    @Override
    public void start(Stage primaryStage) {
        // Create DatabaseManager instance and connect
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.connectToDatabase();

        // --- LOGIN SCREEN ---
        GridPane loginLayout = new GridPane();
        loginLayout.setPadding(new Insets(20));
        loginLayout.setHgap(10);
        loginLayout.setVgap(10);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setStyle("-fx-background-color: black;");

        // Load the logo image
        Image logo = new Image("BatChat.jpg");

        ImageView logoView = new ImageView(logo);
        logoView.setFitHeight(300);
        logoView.setFitWidth(300);
        loginLayout.add(logoView, 0, 0, 2, 1);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-text-fill: #F8F8F8;");
        TextField usernameField = new TextField();
        usernameField.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");

        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-text-fill: #F8F8F8;");
        PasswordField passwordField = new PasswordField();
        passwordField.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #FAEBCD; -fx-text-fill: #434343;");
        Label loginMessage = new Label();
        loginMessage.setStyle("-fx-text-fill: red;");

        loginLayout.add(usernameLabel, 0, 1);
        loginLayout.add(usernameField, 1, 1);
        loginLayout.add(passwordLabel, 0, 2);
        loginLayout.add(passwordField, 1, 2);
        loginLayout.add(loginButton, 1, 3);
        loginLayout.add(loginMessage, 1, 4);

        Scene loginScene = new Scene(loginLayout, 400, 600);

        // --- MAIN SCREEN ---
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: #434343;");

        Label mainLabel = new Label("Select a chat option:");
        mainLabel.setStyle("-fx-text-fill: #F8F8F8;");
        Button privateChatButton = new Button("Private Chat");
        privateChatButton.setStyle("-fx-background-color: #FAEBCD; -fx-text-fill: #434343;");
        Button groupChatButton = new Button("Group Chat");
        groupChatButton.setStyle("-fx-background-color: #FAEBCD; -fx-text-fill: #434343;");
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #FAEBCD; -fx-text-fill: #434343;");

        mainLayout.getChildren().addAll(mainLabel, privateChatButton, groupChatButton, logoutButton);
        Scene mainScene = new Scene(mainLayout, 400, 600);


        loginScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire();
            }
        });

        // --- ONLINE USERS SCREEN ---
        VBox usersLayout = new VBox(10);
        usersLayout.setPadding(new Insets(20));
        usersLayout.setAlignment(Pos.CENTER);

        Label usersLabel = new Label("Online Users:");
        usersLabel.setStyle("-fx-text-fill: #F8F8F8; -fx-font-size: 16px; -fx-font-weight: bold;");
        ListView<String> usersListView = new ListView<>();
        Button backToLoginButton = new Button("Logout");
        backToLoginButton.setStyle("-fx-background-color: #434343; -fx-text-fill: #F8F8F8; -fx-font-size: 14px;");
        usersLayout.getChildren().addAll(usersLabel, usersListView, backToLoginButton);
        usersLayout.setStyle("-fx-background-color: #434343;");
        Scene usersScene = new Scene(usersLayout, 400, 600);

        // --- CHAT SCREEN ---
        ListView<String> messageList = new ListView<>();
        TextField messageField = new TextField();
        messageField.setPromptText("Type a message...");
        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");
        Button attachButton = new Button("📎");
        attachButton.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");
        FileChooser fileChooser = new FileChooser();
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: black; -fx-text-fill: yellow; -fx-font-size: 14px;");

        // Add a header with "BatChat" title
        HBox headerBox = new HBox(100, backButton);
        headerBox.setPadding(new Insets(10));
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-background-color: #434343;");
        headerBox.setPrefHeight(50);

        Button headerTitle = new Button("BatChat");
        headerTitle.setStyle("-fx-background-color: transparent; -fx-text-fill: #F8F8F8; -fx-font-size: 18px;");
        headerTitle.setDisable(true);
        headerBox.getChildren().add(headerTitle);

        HBox inputBox = new HBox(10, attachButton, messageField, sendButton);
        inputBox.setPadding(new Insets(10));
        inputBox.setAlignment(Pos.CENTER);

        BorderPane chatLayout = new BorderPane();
        chatLayout.setTop(headerBox);
        chatLayout.setCenter(messageList);
        chatLayout.setBottom(inputBox);
        chatLayout.setPadding(new Insets(10));
        chatLayout.setStyle("-fx-background-color: #434343;");

        Scene chatScene = new Scene(chatLayout, 400, 600);

// --- GROUP CHAT LIST SCREEN ---
        VBox groupListLayout = new VBox(10);
        groupListLayout.setPadding(new Insets(20));
        groupListLayout.setAlignment(Pos.CENTER);

        Label groupListLabel = new Label("Available Groups:");
        groupListLabel.setStyle("-fx-text-fill: #F8F8F8;");
        ListView<String> groupListView = new ListView<>();
        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Enter group name...");
        Button createGroupButton = new Button("Create Group");
        Button enterGroupButton = new Button("Enter Group");
        Button backToMainButton = new Button("Back");
        groupListLayout.getChildren().addAll(groupListLabel, groupListView, groupNameField, createGroupButton, enterGroupButton, backToMainButton);
        Scene groupListScene = new Scene(groupListLayout, 400, 600);

        // --- GROUP CHATROOM SCREEN ---
        BorderPane groupChatLayout = new BorderPane();
        ListView<String> groupMessageList = new ListView<>();
        TextField groupMessageField = new TextField();
        groupMessageField.setPromptText("Type a message...");
        Button groupSendButton = new Button("Send");
        Button groupBackButton = new Button("Back");


        HBox groupInputBox = new HBox(10, groupMessageField, groupSendButton);
        groupInputBox.setPadding(new Insets(10));
        groupInputBox.setAlignment(Pos.CENTER);

        HBox groupHeaderBox = new HBox(10, groupBackButton);
        groupHeaderBox.setPadding(new Insets(10));
        groupHeaderBox.setAlignment(Pos.CENTER_LEFT);

        groupChatLayout.setTop(groupHeaderBox);
        groupChatLayout.setCenter(groupMessageList);
        groupChatLayout.setBottom(groupInputBox);
        Scene groupChatScene = new Scene(groupChatLayout, 400, 600);

        // --- Login button action ---
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (dbManager.validateLogin(username, password)) {
                currentUser = username;
                loginMessage.setText("");
                primaryStage.setScene(mainScene);
            } else {
                loginMessage.setText("Invalid username or password!");
            }
        });

        // --- Logout button action ---
        logoutButton.setOnAction(e -> {
            currentUser = null;
            primaryStage.setScene(loginScene);
        });

        // --- Group chat button action ---
        groupChatButton.setOnAction(e -> {
            List<String> groups = dbManager.getAllGroups();
            groupListView.getItems().setAll(groups);
            primaryStage.setScene(groupListScene);
        });


        // --- Create group button action ---
        createGroupButton.setOnAction(e -> {
            String groupName = groupNameField.getText().trim();
            if (!groupName.isEmpty() && dbManager.createGroupChat(groupName, currentUser)) { // Updated method for creating group chats
                groupListView.getItems().add(groupName);
                groupNameField.clear();
            }
        });


        // --- Enter group button action ---
        enterGroupButton.setOnAction(e -> {
            String selectedGroup = groupListView.getSelectionModel().getSelectedItem(); // Get selected group
            if (selectedGroup != null) {
                groupMessageList.getItems().clear(); // Clear the group message list
                List<String> groupMessages = dbManager.getGroupChatMessages(selectedGroup); // Updated method for fetching group chat messages
                groupMessageList.getItems().setAll(groupMessages);

                groupSendButton.setOnAction(ev -> {
                    String message = groupMessageField.getText().trim();
                    if (!message.isEmpty()) {
                        dbManager.saveGroupMessage(selectedGroup, currentUser, message); // Updated method for saving group messages
                        groupMessageList.getItems().add(currentUser + ": " + message);  // Update UI
                        groupMessageField.clear();                                     // Clear input field
                    }
                });

                groupBackButton.setOnAction(ev -> primaryStage.setScene(groupListScene)); // Back to group list
                primaryStage.setScene(groupChatScene);                                    // Transition to group chat scene
            }
        });


        // --- Back to main button action ---
        backToMainButton.setOnAction(e -> primaryStage.setScene(mainScene));
        // --- Private Chat List Screen ---
        VBox privateChatListLayout = new VBox(10);
        privateChatListLayout.setPadding(new Insets(20));
        privateChatListLayout.setAlignment(Pos.CENTER);

        Label privateChatListLabel = new Label("Available Contacts:");
        privateChatListLabel.setStyle("-fx-text-fill: #F8F8F8;");
        ListView<String> privateChatListView = new ListView<>();
        Button privateChatBackButton = new Button("Back");
        privateChatBackButton.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");
        privateChatListLayout.getChildren().addAll(privateChatListLabel, privateChatListView, privateChatBackButton);
        privateChatListLayout.setStyle("-fx-background-color: #434343;");
        Scene privateChatListScene = new Scene(privateChatListLayout, 400, 600);

// --- Private Chat Button Action ---
        privateChatButton.setOnAction(e -> {
            List<String> contacts = dbManager.getOnlineUsers(currentUser); // Fetch all contacts
            privateChatListView.getItems().setAll(contacts);    // Populate the list
            primaryStage.setScene(privateChatListScene);        // Switch to private chat list scene
        });

// --- Private Chat List Actions ---
        privateChatListView.setOnMouseClicked(event -> {
            String selectedContact = privateChatListView.getSelectionModel().getSelectedItem();
            if (selectedContact != null) {
                messageList.getItems().clear();                     // Clear the message list
                List<String> chatHistory = dbManager.getChatHistory(currentUser, selectedContact); // Fetch chat history
                messageList.getItems().setAll(chatHistory);

                sendButton.setOnAction(ev -> {
                    String message = messageField.getText().trim();
                    if (!message.isEmpty()) {
                        dbManager.saveMessage(currentUser, selectedContact, message); // Save message
                        messageList.getItems().add(currentUser + ": " + message); // Using currentUser as the sender
                        messageField.clear();                                        // Clear input field
                    }
                });

                backButton.setOnAction(ev -> primaryStage.setScene(privateChatListScene)); // Back to private chat list
                primaryStage.setScene(chatScene);                                         // Switch to private chat scene
            }

            attachButton.setOnAction(e -> {
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
                File file = fileChooser.showOpenDialog(primaryStage);

                if (file != null) {
                    try {
                        // Define the target directory
                        File targetDir = new File("src/main/resources/attachment");
                        if (!targetDir.exists()) {
                            targetDir.mkdirs(); // Create directories if they do not exist
                        }

                        // Copy the file to the target directory
                        File targetFile = new File(targetDir, file.getName());
                        Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                        // Save the message and get the generated messageID
                        String placeholderMessage = "[Attachment]";
                        int messageID = dbManager.saveMessage(currentUser, selectedContact, placeholderMessage);

                        if (messageID != -1) {
                            // Save the attachment
                            String filePath = targetFile.getPath();
                            String fileType = Files.probeContentType(file.toPath());
                            int fileSize = (int) file.length() / 1024; // Size in KB

                            dbManager.saveAttachment(messageID, filePath, fileType, fileSize);

                            // Display in the UI
                            messageList.getItems().add("You attached: " + file.getName());
                        } else {
                            System.err.println("Failed to save message. Attachment not saved.");
                        }
                    } catch (IOException ex) {
                        System.err.println("Error uploading file: " + ex.getMessage());
                    }
                }
            });
        });




// --- Back Button in Private Chat List ---
        privateChatBackButton.setOnAction(e -> primaryStage.setScene(mainScene));
        // --- Users list action ---
        usersListView.setOnMouseClicked(event -> {
            String selectedUser = usersListView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                messageList.getItems().clear();
                List<String> chatHistory = dbManager.getChatHistory(currentUser, selectedUser);
                messageList.getItems().setAll(chatHistory);

                sendButton.setOnAction(ev -> {
                    String message = messageField.getText().trim();
                    if (!message.isEmpty()) {
                        dbManager.saveMessage(currentUser, selectedUser, message);
                        messageList.getItems().add("You: " + message);
                        messageField.clear();
                    }
                });

                backButton.setOnAction(ev -> primaryStage.setScene(usersScene));
                primaryStage.setScene(chatScene);
            }
        });

        primaryStage.setTitle("BatChat");
        primaryStage.setScene(loginScene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> dbManager.closeConnection());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
