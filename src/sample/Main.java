package sample;

import functionality.FXDialog;
import functionality.XMPPManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jxmpp.stringprep.XmppStringprepException;


import java.io.IOException;

public class Main extends Application {

    private Scene scene;
    private Stage window;
    private XMPPManager xmppManager;
    private TextField phone;
    private PasswordField password;
    private MainStage mainStage;
    private RegisterScene registerScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        xmppManager = new XMPPManager("localhost", 5222);
        xmppInitialize();
        window = primaryStage;
        window.setTitle("ChatApp");
        phone = new TextField();
        phone.setPromptText("phone number");
        password = new PasswordField();
        password.setPromptText("password");
        Button login = new Button("Log In");
        login.setOnAction(e -> performLogIn());
        Button signin = new Button("Sign In");
        HBox hbox3 = new HBox(10);
        hbox3.getChildren().addAll(login, signin);
        hbox3.setAlignment(Pos.CENTER);
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(phone, password, hbox3);
        scene = new Scene(vbox, 400, 500);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
        signin.setOnAction(e -> performSignIn());
        window.setScene(scene);
        window.show();
        window.setOnCloseRequest(e -> {
            e.consume();
            xmppManager.destroy();
            window.close();
        });
    }

    public void xmppInitialize() {
        try {
            xmppManager.init();
        } catch (XMPPException | XmppStringprepException e) {
            FXDialog.showError("Connection Error!", "Unable to connect to Server!");
            e.printStackTrace();
        }
    }

    public void performLogIn() {
        try {
            xmppManager.performLogin(phone.getText(), password.getText());
            mainStage = new MainStage(xmppManager);
            mainStage.addToList();
            mainStage.showMainStage(window);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            FXDialog.showError("Login Error", "Please try again");
            xmppInitialize();
            e.printStackTrace();
        }
    }

    public void performSignIn() {
        registerScene = new RegisterScene(window, scene, xmppManager);
        registerScene.showScene();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
