package sample;

import functionality.ChatMessage;
import functionality.FXDialog;
import functionality.User;
import functionality.XMPPManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import sun.util.logging.PlatformLogger;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Akysh on 7/2/2017.
 */
public class ChatPanel implements Serializable {

    private Scene chatScene;
    private Stage stage;
    private VBox vbox, vbox1;
    private HBox labelBox, hbox1, hbox, typingHbox;
    private TextField message;
    private XMPPManager xmppManager;
    private MainStage mainStage;
    private FriendsPanel friendsPanel;
    private String reciever;
    private Label label, label1;
    private Text contactname;
    private BorderPane layout;
    private DateFormat dateFormat;
    private ScrollPane scroll;
    private Map<String, List<ChatMessage>> messages;
    private Map<String, User> sortedUsers;
    private List<ChatMessage> list;
    private List<String> users;
    private Date date;
    private Button button;
    private User user;
    private Circle circle;
    private Rectangle typing;
    private DropShadow dropShadow;
    private Insets insets, insets1;

    public ChatPanel(Stage stage, Scene mainscene, XMPPManager xmppManager, MainStage mainStage, FriendsPanel friendsPanel) {
        this.stage = stage;
        createChatPanel(mainscene);
        this.xmppManager = xmppManager;
        this.mainStage = mainStage;
        this.friendsPanel = friendsPanel;
        messages = new HashMap<>();
        users = new ArrayList<>();
        sortedUsers = new HashMap<>();
        dateFormat = new SimpleDateFormat("HH:mm");
        dropShadow = new DropShadow(+25d, 0d, 0d, Color.DARKGREEN);
    }

    public void createChatPanel(Scene mainscene) {
        message = new TextField();
        message.setPromptText("message");
        message.setMinWidth(300);
        message.setOnAction(e -> sendMessage());
        message.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (message.getText().length() > 0) {
                    xmppManager.sendNotification("composing");
                } else {
                    xmppManager.sendNotification("cancelled");
                }
            }
        });
        Button send = new Button("Send");
        Button emoji = new Button("Emoji");
        Button back = new Button("<");
        back.setOnAction(e -> {
            stage.setScene(mainscene);
        });
        back.setMinWidth(25);
        contactname = new Text("Contact");
        label = new Label();
        label.getStyleClass().add("label-custom");
        insets = new Insets(10, 5, 3, 10);
        insets1 = new Insets(10, 10, 0, 10);
        circle = new Circle(20, 20, 20);
        typing = new Rectangle();
        typing.setWidth(50);
        typing.setHeight(40);
        typing.setArcWidth(35);
        typing.setArcHeight(35);
        typing.setFill(new ImagePattern(new Image(getClass().getResource("jumpingDots.gif").toExternalForm())));
        typingHbox = new HBox(typing);
        typingHbox.setAlignment(Pos.CENTER_LEFT);
        send.setOnAction(e -> sendMessage());
        back.setOnAction(e -> stage.setScene(mainscene));
        vbox = new VBox(label, contactname);
        vbox.setMinWidth(320);
        vbox.setAlignment(Pos.CENTER);
        hbox1 = new HBox(back, vbox, circle);
        hbox1.setPadding(insets);
        HBox hbox2 = new HBox(5);
        hbox2.getChildren().addAll(emoji, message, send);
        scroll = new ScrollPane();
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        layout = new BorderPane();
        layout.setCenter(scroll);
        layout.setTop(hbox1);
        layout.setBottom(hbox2);
        chatScene = new Scene(layout, 400, 500);
        chatScene.getStylesheets().add(getClass().getResource("Chat.css").toExternalForm());
        com.sun.javafx.util.Logging.getCSSLogger().setLevel(PlatformLogger.Level.OFF);
    }

    public void addToChatsHash(User user) {
        if (!sortedUsers.containsKey(user.getUser())) {
            String userName = user.getUser();
            VCard vCard = user.getvCard();
            Circle circ = new Circle(20, 20, 20);
            circ.setFill(user.getAvatar());
            if (xmppManager.getPresence(userName).equals(Presence.Type.available)) {
                user.setActive(true);
                circ.setEffect(dropShadow);
            } else {
                user.setActive(false);
            }
            vbox = new VBox(new Text(vCard.getNickName()), new Label());
            vbox = new VBox(new Text(vCard.getNickName()), new Label());
            vbox.setPrefWidth(250);
            hbox = new HBox(circ, vbox, new Text());
            hbox.setSpacing(5);
            button = new Button("", hbox);
            button.getStyleClass().add("button-custom2");
            button.setOnAction(e -> {
                setVbox(userName);
                setContact(user);
                setReciever(userName);
                showChatPanel();
            });
            vbox = new VBox(5);
            vbox.setPrefWidth(390);
            vbox.setPadding(insets1);
            user.setButtonAvatarVbox(button, vbox);
            sortedUsers.put(userName, user);
            friendsPanel.makeBranch(sortedUsers.values().stream().collect(Collectors.toList()));
        }
    }

    public void setAvatarUpdate(String user) {
        VCard vCard = xmppManager.getVcardOfUser(user);
        byte[] avatar = vCard.getAvatar();
        User user1 = sortedUsers.get(user);
        user1.setAvatar(new Image(new ByteArrayInputStream(avatar)));
        user1.setvCard(vCard);
        HBox hBox = (HBox) user1.getButton().getGraphic();
        Circle circle = (Circle) hBox.getChildren().get(0);
        circle.setFill(user1.getAvatar());
        mainStage.addToList();
        friendsPanel.makeBranch(sortedUsers.values().stream().collect(Collectors.toList()));
    }

    public void sendMessage() {
        String messageText = message.getText();
        try {
            date = new Date();
            xmppManager.sendMessage(messageText, reciever);
            labelBox = new HBox(new VBox(new Text(), new Text(dateFormat.format(date))), new Label(messageText));
            labelBox.setSpacing(5);
            labelBox.setAlignment(Pos.CENTER_RIGHT);
            setMessageDate(reciever, messageText, date, true);
            mainStage.addToList();
            setChatMessage(messageText, date, true);
            message.clear();
        } catch (Exception e) {
            e.printStackTrace();
            FXDialog.showError("Error sending message", "Unable to send message");
        }
    }

    public void showMessage(String message, String from) {
        Label label = new Label(message);
        label.getStyleClass().add("label-custom2");
        date = new Date();
        labelBox = new HBox(label, new Text(dateFormat.format(date)));
        labelBox.setSpacing(5);
        setMessageDate(from, message, date, false);
        mainStage.addToList();
        if (stage.getScene().equals(chatScene)) {
            xmppManager.sendNotification("displayed");
        }
        setChatMessage(message, date, false);
        removeTyping();
    }

    public void setTyping(String from) {
        if (stage.getScene().equals(chatScene)) {
            vbox = sortedUsers.get(from).getvBox();
            if (!vbox.getChildren().contains(typingHbox)) {
                vbox.getChildren().add(typingHbox);
            }
        }
    }

    public void removeTyping() {
        if (stage.getScene().equals(chatScene)) {
            vbox = sortedUsers.get(reciever).getvBox();
            if (vbox.getChildren().contains(typingHbox)) {
                vbox.getChildren().remove(typingHbox);
            }
        }
    }

    public void setMessageDate(String from, String message, Date date, boolean isMine) {
        if (!message.equals(null)) {
            user = sortedUsers.get(from);
            user.getvBox().getChildren().add(labelBox);
            hbox = (HBox) user.getButton().getGraphic();
            vbox = (VBox) hbox.getChildren().get(1);
            label1 = (Label) vbox.getChildren().get(1);
            label1.setText(message);
            if (!isMine) {
                label1.setStyle("-fx-font-weight: bold");
            }
            Text text = (Text) hbox.getChildren().get(2);
            text.setText(dateFormat.format(date));
            user.setDate(date);
        }
    }

    public void setChatMessage(String message, Date date, boolean isMine) {
        if (!messages.containsKey(reciever)) {
            list = new ArrayList<>();
            list.add(new ChatMessage(message, date, isMine));
            messages.put(reciever, list);
        } else {
            messages.get(reciever).add(new ChatMessage(message, date, isMine));
        }
    }

    public void setMessageStatus(String user1, String status) {
        sortedUsers.get(user1).getvBox().getChildren().stream().forEach(s -> {
            HBox hBox = (HBox) s;
            if (hBox.getAlignment().equals(Pos.CENTER_RIGHT)) {
                VBox vBox = (VBox) hBox.getChildren().get(0);
                Text text = (Text) vBox.getChildren().get(0);
                if (!text.getText().equals("Read")) {
                    text.setText(status);
                }
            }
        });
    }

    public void setVbox(String user) {
        vbox = sortedUsers.get(user).getvBox();
        scroll.setContent(vbox);
        scroll.vvalueProperty().bind(vbox.heightProperty());
    }

    public void showChatPanel() {
        stage.setScene(chatScene);
        initializeMessaging(reciever);
        xmppManager.sendNotification("displayed");
        mainStage.setIToZero();
    }

    public void presenceChanged(String user1, Presence.Type type) {
        user = sortedUsers.get(user1);
        hbox = (HBox) user.getButton().getGraphic();
        Circle circ = (Circle) hbox.getChildren().get(0);
        if (type.equals(Presence.Type.available)) {
            circ.setEffect(dropShadow);
            user.setActive(true);
        } else {
            circ.setEffect(null);
            user.setActive(false);
        }
        if (stage.getScene().equals(chatScene)) {
            if (label.getText().equals(user.getvCard().getNickName())) {
                setPresenceForCurrentContact(user);
            }
        }
    }

    public void setContact(User user) {
        label.setText(user.getvCard().getNickName());
        circle.setFill(user.getAvatar());
        setPresenceForCurrentContact(user);
    }

    public void setPresenceForCurrentContact(User user) {
        if (user.getActive()) {
            contactname.setText("Active now");
            circle.setEffect(dropShadow);
        } else {
            circle.setEffect(null);
            contactname.setText(xmppManager.getLastActivity(user.getUser()));
        }
    }

    public void setLabelToNormal(String from) {
        hbox = (HBox) sortedUsers.get(from).getButton().getGraphic();
        vbox = (VBox) hbox.getChildren().get(1);
        label1 = (Label) vbox.getChildren().get(1);
        label1.setStyle("-fx-font-weight: normal");
    }

    public void initializeMessaging(String user) {
        try {
            xmppManager.sendMessage(null, user);
        } catch (Exception e) {
            FXDialog.showError("Error!", "Unable to initialize messaging!");
        }
    }


    public void removeFriend(String user) {
        sortedUsers.remove(user);
        friendsPanel.makeBranch(sortedUsers.values().stream().collect(Collectors.toList()));
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }


    public List<Button> getButtons() {
        List<User> list = sortedUsers.values().stream().collect(Collectors.toList());
        Collections.sort(list);
        list.stream().forEach(e -> {
            System.out.println(e.getUser() + " " + e.getDate());
        });
        return list.stream().map(e -> e.getButton()).collect(Collectors.toList());
    }
}

