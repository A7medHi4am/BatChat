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

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;
import javafx.scene.Node;

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
        loginLayout.setStyle("-fx-background-color: #434343;");

        // Load the logo image
        Image logo = new Image("/images/BatChat_Logo(no_bg).png");
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

        Button createAccountButton = new Button("Create Account");
        createAccountButton.setStyle("-fx-background-color: #FAEBCD; -fx-text-fill: #434343;");


        loginLayout.add(usernameLabel, 0, 1);
        loginLayout.add(usernameField, 1, 1);
        loginLayout.add(passwordLabel, 0, 2);
        loginLayout.add(passwordField, 1, 2);
        loginLayout.add(loginButton, 1, 3);
        loginLayout.add(loginMessage, 1, 5);
        loginLayout.add(createAccountButton, 1, 4); // Position it below the login button

        Scene loginScene = new Scene(loginLayout, 400, 600);
// --- CREATE ACCOUNT SCREEN ---
        VBox createAccountLayout = new VBox(10);
        createAccountLayout.setPadding(new Insets(20));
        createAccountLayout.setAlignment(Pos.CENTER);
        createAccountLayout.setStyle("-fx-background-color: #434343;");

        Label firstNameLabel = new Label("First Name:");
        firstNameLabel.setStyle("-fx-text-fill: #F8F8F8;");
        TextField firstNameField = new TextField();
        firstNameField.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");

        Label lastNameLabel = new Label("Last Name:");
        lastNameLabel.setStyle("-fx-text-fill: #F8F8F8;");
        TextField lastNameField = new TextField();
        lastNameField.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");

        Label createUsernameLabel = new Label("Username:");
        createUsernameLabel.setStyle("-fx-text-fill: #F8F8F8;");
        TextField createUsernameField = new TextField();
        createUsernameField.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");

        Label createPasswordLabel = new Label("Password:");
        createPasswordLabel.setStyle("-fx-text-fill: #F8F8F8;");
        PasswordField createPasswordField = new PasswordField();
        createPasswordField.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");

        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.setStyle("-fx-text-fill: #F8F8F8;");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");

        Button createAccountSubmitButton = new Button("Create Account");
        createAccountSubmitButton.setStyle("-fx-background-color: #FAEBCD; -fx-text-fill: #434343;");

        Button createAccbackButton = new Button("Back");
        createAccbackButton.setStyle("-fx-background-color: #FAEBCD; -fx-text-fill: #434343;");

        Label accountMessage = new Label();
        accountMessage.setStyle("-fx-text-fill: red;");

        createAccountLayout.getChildren().addAll(
                firstNameLabel, firstNameField,
                lastNameLabel, lastNameField,
                createUsernameLabel, createUsernameField,
                createPasswordLabel, createPasswordField,
                confirmPasswordLabel, confirmPasswordField,
                createAccountSubmitButton,
                createAccbackButton,
                accountMessage
        );

        Scene createAccountScene = new Scene(createAccountLayout, 400, 600);

// --- ACTION HANDLERS ---

// Go to Create Account screen
        createAccountButton.setOnAction(event -> {
            primaryStage.setScene(createAccountScene);
        });

// Handle account creation
        createAccountSubmitButton.setOnAction(event -> {
            String newName = createUsernameField.getText().trim();
            String newPass = createPasswordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();

            if (!newPass.equals(confirmPassword)) {
                accountMessage.setText("Passwords do not match!");
            } else if (newName.isEmpty() || newPass.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                accountMessage.setText("All fields must be filled!");
            } else if (dbManager.usernameExists(newName)) {
                accountMessage.setText("Username already exists!");
            } else {
                User newUser = new User(newName, newPass, firstName, lastName);
                boolean success = dbManager.createUser(newUser);  // Attempt to create the user

                if (success) {
                    accountMessage.setText("Account created successfully!");
                    primaryStage.setScene(loginScene); // Go back to the login page
                } else {
                    accountMessage.setText("Account creation failed! Please try again.");
                }
            }
        });

// Handle back button action
        createAccbackButton.setOnAction(event -> {
            primaryStage.setScene(loginScene); // Go back to the login page
        });

// Set the initial scene to login screen
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("BatChat");
        primaryStage.show();

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

        // --- CHAT SCREEN ---
        ListView<Node> messageList = new ListView<>();
        TextField messageField = new TextField();
        messageField.setPromptText("Type a message...");
        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");
        Button attachButton = new Button("📎");
        attachButton.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");
        FileChooser fileChooser = new FileChooser();
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #f7c873; -fx-text-fill: #434343; -fx-font-size: 14px;");

        // Add a header with "BatChat" title
        HBox headerBox = new HBox(100, backButton);
        headerBox.setPadding(new Insets(10));
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-background-color: #434343;");
        headerBox.setPrefHeight(50);

        Label headerTitle = new Label("BatChat");
        headerTitle.setStyle("-fx-background-color: transparent; -fx-text-fill: #F8F8F8; -fx-font-size: 18px;");
        //headerTitle.setDisable(true);
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

        chatScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendButton.fire();
            }
        });

        // --- GROUP CHAT LIST SCREEN ---
        VBox groupListLayout = new VBox(10);
        groupListLayout.setPadding(new Insets(20));
        groupListLayout.setAlignment(Pos.CENTER);
        groupListLayout.setStyle("-fx-background-color: #434343;");

        Label groupListLabel = new Label("Available Groups:");
        groupListLabel.setStyle("-fx-text-fill: #F8F8F8;");
        ListView<String> groupListView = new ListView<>();
        groupListView.setStyle("-fx-background-color: #f7c873;");

        Button createGroupButton = new Button("Create Group");
        createGroupButton.setStyle("-fx-background-color: #f7c873; -fx-text-fill: #434343; -fx-font-size: 14px;");

        Button backToMainButton = new Button("Back");
        backToMainButton.setStyle("-fx-background-color: #f7c873; -fx-text-fill: #434343; -fx-font-size: 14px;");
        groupListLayout.getChildren().addAll(groupListLabel, groupListView, createGroupButton, backToMainButton);
        Scene groupListScene = new Scene(groupListLayout, 400, 600);


        // --- GROUP CHATROOM SCREEN ---
        BorderPane groupChatLayout = new BorderPane();
        ListView<Node> groupMessageList = new ListView<>(); // Use ListView<Node> for text and images
        TextField groupMessageField = new TextField();
        groupMessageField.setPromptText("Type a message...");
        Button groupSendButton = new Button("Send");
        Button groupBackButton = new Button("Back");
        Button groupAttachButton = new Button("📎"); // New attach button for group chat

        HBox groupInputBox = new HBox(10, groupMessageField, groupSendButton, groupAttachButton); // Include attach button
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
            usernameField.clear(); // Clear the username field
            passwordField.clear(); // Clear the password field


        });

        // --- Logout button action ---
        logoutButton.setOnAction(e -> {
            currentUser = null;
            primaryStage.setScene(loginScene);
        });



// --- Group chat button action ---
        groupChatButton.setOnAction(e -> {
            List<GroupChat> groupChats = dbManager.getUserGroups(currentUser);

            // Use Collectors to map GroupChat objects to their RoomNames (Strings)
            List<String> groupNames = groupChats.stream()
                    .map(GroupChat::getRoomName) // Get the RoomName of each GroupChat
                    .collect(Collectors.toList()); // Collect them into a list of strings

            groupListView.getItems().setAll(groupNames);

            primaryStage.setScene(groupListScene);
        });

// --- Create group button action ---
        createGroupButton.setOnAction(e -> {
            // Prompt to enter the room name
            TextInputDialog roomNameDialog = new TextInputDialog();
            roomNameDialog.setHeaderText("Create Group Chat");
            roomNameDialog.setContentText("Enter Room Name:");
            Optional<String> roomResult = roomNameDialog.showAndWait();

            roomResult.ifPresent(roomName -> {
                if (roomName.isEmpty()) {
                    showAlert("Error", "Group Chat Name cannot be empty!");
                    return;
                }

                // Create a new layout for adding participants
                VBox participantLayout = new VBox(10);
                participantLayout.setPadding(new Insets(10));
                participantLayout.setAlignment(Pos.CENTER);

                Label participantLabel = new Label("Add Participants:");
                participantLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                ListView<String> participantListView = new ListView<>();
                TextField participantField = new TextField();
                participantField.setPromptText("Enter username to add...");
                Button addParticipantButton = new Button("Add");
                addParticipantButton.setStyle("-fx-background-color: #FAEBCD; -fx-text-fill: #434343;");

                Button createGroupFinalButton = new Button("Create Group");
                createGroupFinalButton.setStyle("-fx-background-color: #FAEBCD; -fx-text-fill: #434343;");

                // Layout organization
                participantLayout.getChildren().addAll(participantLabel, participantListView, participantField, addParticipantButton, createGroupFinalButton);
                Scene participantScene = new Scene(participantLayout, 400, 400);

                Stage participantStage = new Stage();
                participantStage.setScene(participantScene);
                participantStage.setTitle("Add Users");
                participantStage.show();

                List<String> selectedUsers = new ArrayList<>();

                // Handle adding participants
                addParticipantButton.setOnAction(ev -> {
                    String enteredUsername = participantField.getText().trim();
                    if (enteredUsername.isEmpty()) {
                        showAlert("Error", "Username CANNOT be empty!");
                    } else if (dbManager.usernameExists(enteredUsername)) {
                        if (!selectedUsers.contains(enteredUsername)) {
                            selectedUsers.add(enteredUsername);
                            participantListView.getItems().add(enteredUsername);
                            participantField.clear();
                        } else {
                            showAlert("Info", "User '" + enteredUsername + "' is already added.");
                        }
                    } else {
                        showAlert("Error", "User '" + enteredUsername + "' not found.");
                    }
                });

                // Handle creating the group
                createGroupFinalButton.setOnAction(ev -> {
                    if (selectedUsers.size() < 2) {
                        showAlert("Error", "A group chat must have at least 3 users");
                        return;
                    }

                    if (dbManager.createGroupChat(roomName, currentUser)) {
                        selectedUsers.forEach(username -> dbManager.addParticipantToGroupChat(roomName, username));
                        showAlert("Success", "Group " + roomName + " created successfully.");
                        groupListView.getItems().add(roomName);
                        participantStage.close();
                    } else {
                        showAlert("Error", "Failed to create the group.");
                    }
                });
            });
        });

// --- Handle group chat selection from the groupListView ---
        groupListView.setOnMouseClicked(event -> {
            String selectedGroup = groupListView.getSelectionModel().getSelectedItem();
            if (selectedGroup != null) {
                // Clear the group message list before loading new content
                groupMessageList.getItems().clear();

                // Fetch chat history for the selected group
                List<Message> groupMessages = dbManager.getGroupChatMessages(selectedGroup);

                // Populate the message list with formatted messages
                for (Message message : groupMessages) {
                    String messageContent = message.getContent();
                    File file = new File(messageContent);

                    if (file.exists()) { // Check if it's a valid file path
                        Label senderLabel = new Label(message.getSender() + ": ");
                        groupMessageList.getItems().add(senderLabel);

                        Image image = new Image("file:" + file.getPath());
                        ImageView imageView = new ImageView(image);
                        imageView.setFitHeight(100);
                        imageView.setPreserveRatio(true);
                        groupMessageList.getItems().add(imageView);
                    } else {
                        groupMessageList.getItems().add(new Label(message.getSender() + ": " + messageContent));
                    }
                }

                // Set up message sending
                groupSendButton.setOnAction(ev -> {
                    String messageContent = groupMessageField.getText().trim();
                    if (!messageContent.isEmpty()) {
                        dbManager.saveGroupMessage(selectedGroup, currentUser, messageContent); // Save text message
                        groupMessageList.getItems().add(new Label(currentUser + ": " + messageContent));
                        groupMessageField.clear();
                    }
                });

                // Handle file attachment
                groupAttachButton.setOnAction(ev -> {
                    FileChooser groupFileChooser = new FileChooser();
                    groupFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
                    File file = groupFileChooser.showOpenDialog(primaryStage);
                    if (file != null) {
                        try {
                            // Save file to attachment directory
                            File targetDir = new File("resources/attachment");
                            if (!targetDir.exists()) {
                                targetDir.mkdirs();
                            }

                            File targetFile = new File(targetDir, file.getName());
                            Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                            // Save the file path as the message content
                            String filePath = targetFile.getPath();
                            int messageID = dbManager.saveGroupMessageWithAttachment(selectedGroup, currentUser, filePath, filePath);

                            if (messageID != -1) {
                                // Save attachment metadata
                                String fileType = Files.probeContentType(file.toPath());
                                int fileSize = (int) file.length() / 1024; // File size in KB
                                dbManager.saveAttachment(messageID, filePath, fileType, fileSize);

                                // Display the attachment in the chat
                                Label senderLabel = new Label(currentUser + ": ");
                                groupMessageList.getItems().add(senderLabel);

                                Image image = new Image("file:" + filePath);
                                ImageView imageView = new ImageView(image);
                                imageView.setFitHeight(100);
                                imageView.setPreserveRatio(true);
                                groupMessageList.getItems().add(imageView);
                            } else {
                                System.err.println("Failed to save group message. Attachment not saved.");
                            }
                        } catch (IOException ex) {
                            System.err.println("Error uploading file: " + ex.getMessage());
                        }
                    }
                });

                // Back button to go back to the group list
                groupBackButton.setOnAction(ev -> primaryStage.setScene(groupListScene));

                // Transition to the selected group chat scene
                primaryStage.setScene(groupChatScene);
            }
        });

// Handle keyboard events for Enter and Back buttons
        groupChatScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (groupSendButton.isFocused()) {
                    groupSendButton.fire();
                } else if (groupBackButton.isFocused()) {
                    groupBackButton.fire();
                }
            }
        });

        // --- Back to main button action ---
        backToMainButton.setOnAction(e -> primaryStage.setScene(mainScene));
        // --- Private Chat List Screen ---
        VBox privateChatListLayout = new VBox(10);
        privateChatListLayout.setPadding(new Insets(20));
        privateChatListLayout.setAlignment(Pos.CENTER);

        // Label for available contacts
        Label privateChatListLabel = new Label("Available Users:");
        privateChatListLabel.setStyle("-fx-text-fill: #F8F8F8;");

        // ListView for contacts
        ListView<String> privateChatListView = new ListView<>();

        // Buttons for navigation
        Button privateChatBackButton = new Button("Back");
        privateChatBackButton.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");
        Button newChatButton = new Button("New Chat");
        newChatButton.setStyle("-fx-background-color: #F7C873; -fx-text-fill: #434343;");

        // Organize layout with spacing and padding
        privateChatListLayout.getChildren().addAll(privateChatListLabel, privateChatListView, newChatButton, privateChatBackButton);
        privateChatListLayout.setStyle("-fx-background-color: #434343;");

        // Create the scene for the private chat list
        Scene privateChatListScene = new Scene(privateChatListLayout, 400, 600);


        // --- Private Chat Button Action ---
        privateChatButton.setOnAction(e -> {
            List<User> contacts = dbManager.getChatContacts(currentUser);

            List<String> usernames = contacts.stream()
                    .map(User::getUsername)
                    .collect(Collectors.toList());

            privateChatListView.getItems().setAll(usernames);
            primaryStage.setScene(privateChatListScene);

        });


        // --- New Chat Button Action ---
        newChatButton.setOnAction(e -> {
            // Prompt to enter the recipient's username
            TextInputDialog usernameDialog = new TextInputDialog();
            usernameDialog.setHeaderText("Start New Private Chat");
            usernameDialog.setContentText("Enter recipient username:");
            Optional<String> usernameResult = usernameDialog.showAndWait();

            usernameResult.ifPresent(recipientUsername -> {
                if (recipientUsername.isEmpty()) {
                    showAlert("Error", "Username CANNOT be empty!");
                    return;
                }

                // Check if the recipient username exists
                if (!dbManager.usernameExists(recipientUsername)) {
                    showAlert("Error", "User not found.");
                    return;
                }

                // Check if a chat already exists with this user
                if (dbManager.createPrivateChat(currentUser, recipientUsername)) {
                    // Successfully created the chat
                    showAlert("Success", "New chat created with " + recipientUsername);

                    // Add the new chat to the list of available contacts
                    List<User> contacts = dbManager.getChatContacts(currentUser);
                    List<String> usernames = contacts.stream()
                            .map(User::getUsername)
                            .collect(Collectors.toList());
                    privateChatListView.getItems().setAll(usernames);

                } else {
                    showAlert("Error", "Failed to create a private chat with " + recipientUsername);
                }
            });
        });

        privateChatListView.setOnMouseClicked(event -> {
            String selectedContact = privateChatListView.getSelectionModel().getSelectedItem();
            if (selectedContact != null) {
                // Clear the message list once before adding new content
                messageList.getItems().clear();

                // Fetch chat history for the selected contact
                List<Message> chatHistory = dbManager.getChatHistory(currentUser, selectedContact);

                // Iterate over each message in the chat history
                chatHistory.forEach(message -> {
                    // Check if the message content is a file path (i.e., it represents an attachment)
                    String messageContent = message.getContent();
                    File file = new File(messageContent); // Check if the content is a valid file path

                    // Fetch and display attachments if the content is a file path
                    if (file.exists()) {
                        // Display the sender's name with the attachment
                        Label senderLabel = new Label(message.getSender() + ": ");
                        messageList.getItems().add(senderLabel); // Add sender label to the chat
                        // Display image attachments first
                        Image image = new Image("file:" + file.getPath());
                        ImageView imageView = new ImageView(image);
                        imageView.setFitHeight(100); // Set a fixed height for images
                        imageView.setPreserveRatio(true); // Preserve the image ratio
                        messageList.getItems().add(imageView); // Add image to the message list
                    } else {
                        // If it's not a file path, display the message content (text)
                        Label textLabel = new Label(message.getSender() + ": " + messageContent);
                        messageList.getItems().add(textLabel); // Add message to the chat
                    }


                });
                // Send button action
                sendButton.setOnAction(ev -> {
                    String messageContent = messageField.getText().trim();
                    if (!messageContent.isEmpty()) {
                        int messageID = dbManager.saveMessage(currentUser, selectedContact, messageContent); // Save the text message
                        if (messageID != -1) {
                            Label textLabel = new Label(currentUser + ": " + messageContent);
                            messageList.getItems().add(textLabel); // Display the text message in the chat
                            messageField.clear(); // Clear the input field
                        } else {
                            System.err.println("Failed to save the message.");
                        }
                    }

                });


                // Attachment button action
                attachButton.setOnAction(e -> {
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
                    File file = fileChooser.showOpenDialog(primaryStage);
                    if (file != null) {
                        try {
                            // Create directory to store the file if it doesn't exist
                            File targetDir = new File("resources/attachment");
                            if (!targetDir.exists()) {
                                targetDir.mkdirs(); // Create directories if they don't exist
                            }

                            // Create a target file with the same name as the original file
                            File targetFile = new File(targetDir, file.getName());
                            Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                            // Save the message content as the file path (instead of a placeholder)
                            String messageContent = targetFile.getPath();  // Store the file path in the message content
                            int messageID = dbManager.saveMessage(currentUser, selectedContact, messageContent); // Save the message with file path

                            if (messageID != -1) {
                                // Save the attachment metadata in the database
                                String filePath = targetFile.getPath();
                                String fileType = Files.probeContentType(file.toPath());
                                int fileSize = (int) file.length() / 1024; // File size in KB
                                dbManager.saveAttachment(messageID, filePath, fileType, fileSize);

                                // Display the sender's name first, then the image
                                Label senderLabel = new Label(currentUser + ": ");
                                messageList.getItems().add(senderLabel); // Add sender label to the chat

                                // Display the image attachment
                                Image image = new Image("file:" + filePath);
                                ImageView imageView = new ImageView(image);
                                imageView.setFitHeight(100); // Set a fixed height for images
                                imageView.setPreserveRatio(true); // Preserve image aspect ratio
                                messageList.getItems().add(imageView); // Add image to the message list
                            } else {
                                System.err.println("Failed to save message. Attachment not saved.");
                            }
                        } catch (IOException ex) {
                            System.err.println("Error uploading file: " + ex.getMessage());
                        }
                    }
                });

                // Back button action
                backButton.setOnAction(ev -> primaryStage.setScene(privateChatListScene)); // Back to private chat list
                primaryStage.setScene(chatScene); // Switch to private chat scene
            }
        });


// Handle Enter key for navigation
        privateChatListScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (privateChatBackButton.isFocused()) {
                    privateChatBackButton.fire();
                }
            }
        });

// Back Button in Private Chat List
        privateChatBackButton.setOnAction(e -> primaryStage.setScene(mainScene));
        primaryStage.setScene(loginScene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> dbManager.closeConnection());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
