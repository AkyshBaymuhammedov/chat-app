package sample;

import functionality.AutoCompleteTextField;
import functionality.XMPPManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by Akysh on 7/12/2017.
 */
public class SearchUser {

    private Stage window;
    private Scene searchScene, friendsScene, foundUsers;
    private AutoCompleteTextField searchField;
    private XMPPManager xmppManager;
    private BorderPane layout;
    private String user;
    private Button addFriend;
    private HBox hbox1;
    private VBox vBox;

    public SearchUser(Stage window, Scene friendsScene, XMPPManager xmppManager) {
        this.window = window;
        this.friendsScene = friendsScene;
        this.xmppManager = xmppManager;
        searchField = new AutoCompleteTextField(xmppManager, this);
        searchField.setPromptText("Search");
        searchField.setPrefWidth(335);
        Button cancel = new Button("x");
        cancel.setOnAction(e -> searchField.clear());
        Button back = new Button("<");
        back.setOnAction(e -> window.setScene(friendsScene));
        back.getStyleClass().add("button-custom1");
        addFriend = new Button("+");
        addFriend.setOnAction(e -> xmppManager.addFriend(user));
        HBox hBox = new HBox(5);
        hBox.getChildren().addAll(back, searchField, cancel);
        hBox.setPrefSize(400, 20);
        hBox.setPadding(new Insets(10, 10, 10, 10));
        hBox.setStyle("-fx-background-color: cornflowerblue");
        hbox1 = new HBox(5);
        vBox = new VBox(5);
        vBox.setPadding(new Insets(10, 5, 5, 5));
        layout = new BorderPane();
        layout.setTop(hBox);
        layout.setCenter(vBox);
        searchScene = new Scene(layout, 400, 500);
        searchScene.getStylesheets().add(getClass().getResource("UserSearchStyle.css").toExternalForm());
    }

    public void showSearchUserScene() {
        window.setScene(searchScene);
    }

    public void addFoundUser(String user) {
        this.user = user;
        userExist(user);
        Button userButton = new Button(user);
        userButton.getStyleClass().add("button-custom");
        hbox1.getChildren().clear();
        hbox1.getChildren().addAll(userButton, addFriend);
        vBox.getChildren().clear();
        vBox.getChildren().add(hbox1);
    }

    public void userExist(String user) {
        try {
            xmppManager.printRoster().stream().forEach(s -> {
                if (s.getUser().toString().replace("@akysh.letschat.local", "").equals(user)) {
                    addFriend.setDisable(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
