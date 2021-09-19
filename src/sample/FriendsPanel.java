package sample;

import functionality.User;
import functionality.XMPPManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.File;
import java.util.List;

/**
 * Created by Akysh on 7/2/2017.
 */
public class FriendsPanel {

    private TreeView<HBox> tree;
    private TreeItem<HBox> root, friends, favourites, profile, friendrequests, item, item1;
    private BorderPane bpane;
    private StackPane layout;
    private Button button, accept, deny;
    private HBox buttonBox;
    private XMPPManager xmppManager;
    private Stage avatarStage, showAvatarPic;
    private Circle avatar;
    private String request;

    public FriendsPanel(BorderPane bpane, XMPPManager xmppManager) {
        this.bpane = bpane;
        this.xmppManager = xmppManager;
        createFriendsPanel();
    }

    public void createFriendsPanel() {
        root = new TreeItem<>();
        root.setExpanded(true);
        Label label = new Label("friends");
        label.getStyleClass().add("label-custom");
        friends = new TreeItem<>(new HBox(label));
        friends.setExpanded(true);
        label = new Label("favourites");
        label.getStyleClass().add("label-custom");
        favourites = new TreeItem<>(new HBox(label));
        favourites.setExpanded(true);
        label = new Label("profile");
        label.getStyleClass().add("label-custom");
        profile = new TreeItem<>(new HBox(label));
        profile.setExpanded(true);
        label = new Label("friend requests");
        label.getStyleClass().add("label-custom");
        friendrequests = new TreeItem<>(new HBox(label));
        friendrequests.setExpanded(true);
        User user = xmppManager.getCurrentUser();
        Image im = user.getAvatarImage();
        VCard vCard = user.getvCard();
        avatar = new Circle(20, 20, 20);
        avatar.setFill(user.getAvatar());
        button = new Button(vCard.getNickName(), avatar);
        button.setOnAction(e -> showAvatarStage());
        button.getStyleClass().add("button-custom3");
        accept = new Button("Accept");
        accept.getStyleClass().add("button-custom4");
        accept.setOnAction(e -> {
            xmppManager.acceptRequest(request);
        });
        deny = new Button("Deny");
        deny.setOnAction(e -> {
            xmppManager.denyRequest(request);
        });
        deny.getStyleClass().add("button-custom4");
        TreeItem<HBox> profileitem = new TreeItem<>(new HBox(button));
        profile.getChildren().add(profileitem);
        root.getChildren().addAll(profile, favourites, friendrequests, friends);

        tree = new TreeView<>(root);
        tree.setShowRoot(false);

        layout = new StackPane();
        layout.getChildren().add(tree);
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setPadding(new Insets(10, 10, 10, 10));
        createAvatarSettingsStage(im);
    }

    public void createAvatarSettingsStage(Image image) {
        avatarStage = new Stage();
        Circle circle = new Circle(20, 20, 20);
        circle.setFill(new ImagePattern(image));
        Button changeAvatar = new Button("Change");
        changeAvatar.setOnAction(e -> openFileChooser(circle));
        Button showAvatar = new Button("Show");
        showAvatar.setOnAction(e -> showAvatarPicStage());
        HBox hBox = new HBox(changeAvatar, showAvatar);
        hBox.setSpacing(5);
        hBox.setAlignment(Pos.CENTER);
        VBox vBox = new VBox(circle, hBox);
        vBox.setSpacing(5);
        vBox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vBox, 200, 200);
        avatarStage.setScene(scene);
        showAvatarPic = new Stage();
    }

    public void openFileChooser(Circle circle) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        File selectedFile = fileChooser.showOpenDialog(avatarStage);
        if (selectedFile != null) {
            xmppManager.setAvatar(selectedFile);
            Image image = new Image(selectedFile.toURI().toString());
            avatar.setFill(new ImagePattern(image));
            circle.setFill(new ImagePattern(image));
        }
    }

    public void showAvatarPicStage() {
        ImageView imv = new ImageView(xmppManager.getAvatar());
        StackPane pane = new StackPane(imv);
        Scene scene1 = new Scene(pane);
        showAvatarPic.setScene(scene1);
        showAvatarPic.show();
    }

    public void showAvatarStage() {
        avatarStage.show();
    }

    public void showFriendsPane() {
        bpane.setCenter(layout);
    }

    public void friendRequests(String request) {
        this.request = request;
        button = new Button(request);
        button.getStyleClass().add("button-custom4");
        friendrequests.getChildren().clear();
        friendrequests.getChildren().add(new TreeItem<>(new HBox(button, accept, deny)));
    }

    public void removeRequest(String request) {
        friendrequests.getChildren().stream().forEach(s -> {
            Button button = (Button) s.getValue().getChildren().get(0);
            if (button.getText().equals(request)) {
                item1 = s;
            }
        });
        friendrequests.getChildren().remove(item1);
    }

    public void makeBranch(List<User> usersList) {
        friends.getChildren().clear();
        usersList.stream().forEach(e -> {
            Circle circ = new Circle(20, 20, 20);
            circ.setFill(e.getAvatar());
            button = new Button(e.getvCard().getNickName(), circ);
            button.getStyleClass().add("button-custom3");
            item = new TreeItem<>(new HBox(button));
            friends.getChildren().add(item);
        });
    }


}