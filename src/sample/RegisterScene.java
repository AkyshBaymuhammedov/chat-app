package sample;

import functionality.EmailVerificationField;
import functionality.FXDialog;
import functionality.SendEmail;
import functionality.XMPPManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jxmpp.stringprep.XmppStringprepException;
import validator.EmailValidator;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Akysh on 7/1/2017.
 */
public class RegisterScene {
    private Stage stage, errorStage;
    private Scene oldScene, scene, errorScene, confirmScene;
    private TextField input, phone, nick, fullname, passw, confirmPassword, codeInput;
    private ComboBox<String> gender;
    private EmailValidator valid;
    private MainStage mainStage;
    private int code;
    private XMPPManager xmppManager;
    Random rand;

    public RegisterScene(Stage stage, Scene oldScene, XMPPManager xmppManager) {
        this.stage = stage;
        this.oldScene = oldScene;
        valid = new EmailValidator();
        rand = new Random();
        this.xmppManager = xmppManager;
        createRegisterScene();
        createConfirmScene();
    }


    public void createRegisterScene() {

        phone = new TextField();
        phone.setPromptText("phone number");
        nick = new TextField();
        nick.setPromptText("nickname");
        fullname = new TextField();
        fullname.setPromptText("full name");
        passw = new PasswordField();
        passw.setPromptText("password");
        confirmPassword = new PasswordField();
        confirmPassword.setPromptText("confirm your password");
        input = new EmailVerificationField(xmppManager, valid);
        input.setPromptText("email");
        Label genderLabel = new Label("Gender:");
        gender = new ComboBox<>();
        gender.getItems().addAll("Male", "Female");
        //gender.setPromptText("Select");
        gender.setValue("Male");
        Button register = new Button("Register");
        Button back = new Button("Back");
        back.setOnAction(e -> {
            xmppManager.destroy();
            stage.setScene(oldScene);
        });
        back.setMinWidth(50);
        register.setOnAction(e -> verifyPassword());

        HBox hBox = new HBox(genderLabel, gender);
        hBox.setSpacing(50);
        hBox.setAlignment(Pos.CENTER);
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(phone, input, nick, fullname, passw, confirmPassword, hBox, register);
        vbox.setAlignment(Pos.CENTER);
        BorderPane layout = new BorderPane();
        layout.setTop(back);
        layout.setCenter(vbox);
        layout.setPadding(new Insets(10, 10, 10, 10));
        scene = new Scene(layout, 400, 500);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
        stage.setScene(scene);
    }

    public void createConfirmScene() {
        codeInput = new TextField();
        codeInput.setPromptText("code");
        Label label = new Label("Code:");
        Button confirm = new Button("Confirm");
        confirm.setOnAction(e -> verifyCode());
        Button back = new Button("Back");
        back.setOnAction(e -> stage.setScene(scene));

        HBox hbox = new HBox(5);
        hbox.getChildren().addAll(label, codeInput);
        hbox.setAlignment(Pos.CENTER);

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(hbox, confirm);
        vbox.setAlignment(Pos.CENTER);

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setTop(back);
        layout.setCenter(vbox);

        confirmScene = new Scene(layout, 400, 500);
        confirmScene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
    }

    public void verifyPassword() {
        if (nick.getText().length() == 0 || fullname.getText().length() == 0 || passw.getText().length() == 0 || confirmPassword.getText().length() == 0) {
            return;
        }
        if (!valid.validate(input.getText())) {
            return;
        }
        if (confirmPassword.getText().equals(passw.getText())) {
            sendCode();
            xmppManager.destroy();
        } else {
            confirmPassword.getStyleClass().add("custom");
            passw.getStyleClass().add("custom");
        }
    }

    public void sendCode() {
        code = rand.ints(1, 1111, 9999).findFirst().getAsInt();
        System.out.println(code);
        String email = input.getText();
        Thread t = new Thread(() -> {
            try {
                SendEmail.sendPlainTextEmail(email, "Code", String.valueOf(code));
            } catch (MessagingException | IllegalArgumentException e) {
                Platform.runLater(() -> FXDialog.showError("Error!", "Error sending Email"));
            }
        });
        t.start();
        stage.setScene(confirmScene);
    }

    public void verifyCode() {
        String nickname = nick.getText();
        String password = passw.getText();
        if (Integer.parseInt(codeInput.getText()) == code) {
            createAccount(nickname, password, fullname.getText(), input.getText());
            performLogIn(password);
            mainStage = new MainStage(xmppManager);
            mainStage.addToList();
            mainStage.showMainStage(stage);
        }
    }

    public void createAccount(String nickname, String password, String name, String email) {
        try {
            xmppManager.createAccount(phone.getText(), nickname, password, name, email, gender.getValue());
        } catch (Exception e) {
            FXDialog.showError("Account creating error", "Account already exist!");
            e.printStackTrace();
        }
    }

    public void performLogIn(String password) {
        try {
            xmppManager.performLogin(phone.getText(), password);
            xmppManager.setVcards();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            FXDialog.showError("Login Error", "Please try again");
            try {
                xmppManager.init();
            } catch (XMPPException e1) {
                e1.printStackTrace();
            } catch (XmppStringprepException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void showScene() {
        stage.setScene(scene);
        xmppManager.searcherLogin();
    }
}
