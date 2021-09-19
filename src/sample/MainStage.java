package sample;

import functionality.XMPPManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.jivesoftware.smack.packet.Presence;

/**
 * Created by Akysh on 7/1/2017.
 */
public class MainStage {

    private Stage mainStage;
    private Scene scene;
    private VBox box;
    private Button button, accept, deny, friends, chats, add;
    private ChatPanel chatPanel;
    private FriendsPanel friendsPanel;
    private MorePanel morePanel;
    private XMPPManager xmppManager;
    private SearchUser searchUser;
    private BorderPane layout;
    private ImageView imageView;
    private Integer i = 0;

    public MainStage(XMPPManager xmppManager) {
        this.xmppManager = xmppManager;
        mainStage = new Stage();
        createMainStage();
    }

    public void createMainStage() {
        imageView = new ImageView(new Image(getClass().getResource("rsz_chat-512.png").toExternalForm()));
        chats = new Button();
        chats.setGraphic(new ImageView(new Image(getClass().getResource("rsz_chat-512.png").toExternalForm())));
        friends = new Button();
        friends.setGraphic(new ImageView(new Image(getClass().getResource("rsz_friends.png").toExternalForm())));
        Button more = new Button();
        more.setGraphic(new ImageView(new Image(getClass().getResource("rsz_more.png").toExternalForm())));
        Button edit = new Button();
        Icons btn3 = new Icons(null, createNotification("10"), Icons.AnimationType.BLINK);
        edit.setGraphic(new ImageView(new Image(getClass().getResource("rsz_edit.png").toExternalForm())));
        add = new Button("+");
        add.setOnAction(e -> {
            try {
                searchUser.showSearchUserScene();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        Insets insets = new Insets(10, 10, 10, 10);
        add.getStyleClass().add("button-custom");
        HBox hbox1 = new HBox(305);
        hbox1.getChildren().addAll(edit, add);
        hbox1.setPadding(insets);
        HBox hbox2 = new HBox(20);
        hbox2.getChildren().addAll(friends, chats, more);
        hbox2.setAlignment(Pos.CENTER);
        hbox2.setPadding(insets);

        box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(400);
        box.setPadding(insets);
        ScrollPane pane = new ScrollPane(box);
        layout = new BorderPane();
        layout.setTop(hbox1);
        layout.setBottom(hbox2);
        layout.setCenter(pane);
        scene = new Scene(layout, 400, 500);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
        friendsPanel = new FriendsPanel(layout, xmppManager);
        friends.setOnAction(e -> friendsPanel.showFriendsPane());
        morePanel = new MorePanel(layout);
        more.setOnAction(e -> morePanel.showMorePanel());
        chats.setOnAction(e -> {
            layout.setCenter(pane);
            layout.setTop(hbox1);
        });
        mainStage.setScene(scene);
        xmppManager.setMainStage(this);
        searchUser = new SearchUser(mainStage, scene, xmppManager);
        chatPanel = new ChatPanel(mainStage, scene, xmppManager, this, friendsPanel);
        xmppManager.setChatPanel(chatPanel);
        mainStage.setOnCloseRequest(e -> {
            e.consume();
            xmppManager.destroy();
            mainStage.close();
        });
    }

    public void addToList() {
        long start = System.nanoTime();
        box.getChildren().clear();
        try {
            xmppManager.getFriends().forEach(s -> {
                String user = s.getUser();
                if (xmppManager.getPresence(user) == Presence.Type.subscribe) {
                    friendsPanel.friendRequests(user);
                }
                chatPanel.addToChatsHash(s);
            });
            box.getChildren().addAll(chatPanel.getButtons());
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = System.nanoTime();
        System.out.println((end - start) / 1000000);
    }


    private Node createNotification(String number) {
        StackPane p = new StackPane();
        Label lab = new Label(number);
        lab.setStyle("-fx-text-fill:white");
        Circle circle = new Circle(8, Color.rgb(200, 0, 0, .9));
        circle.setStrokeWidth(1.0);
        circle.setStyle("-fx-background-insets: 0 0 -1 0, 0, 1, 2;");
        circle.setSmooth(true);
        p.setPrefSize(2, 2);
        p.getChildren().addAll(circle, lab);
        return p;
    }

    public void showMainStage(Stage stage) {
        stage.close();
        mainStage.show();
    }

    public void notification() {
        i++;
        chats.setGraphic(new HBox(imageView, new Icons(null, createNotification(i.toString()), Icons.AnimationType.JUMP)));
    }

    public void setIToZero() {
        i = 0;
        chats.setGraphic(imageView);
    }

    public FriendsPanel getFriendsPanel() {
        return friendsPanel;
    }
}
