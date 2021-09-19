package functionality;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Akysh on 7/28/2017.
 */
public class User implements Serializable, Comparable<User> {

    private String user, name, email;
    private ImagePattern avatar;
    private Image avatarImage;
    private VCard vCard;
    private Button button;
    private VBox vBox;
    private Date date;
    private boolean active;

    public User(String user, String name) {
        this.user = user;
        this.name = name;
        date = new Date();
    }

    public String getUser() {
        return user;
    }

    public String getName() {
        return name;
    }


    public void setvCard(VCard vCard) {
        this.vCard = vCard;
    }

    public VCard getvCard() {
        return vCard;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public Button getButton() {
        return button;
    }

    public void setvBox(VBox vBox) {
        this.vBox = vBox;
    }

    public VBox getvBox() {
        return vBox;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getActive() {
        return active;
    }

    public void setAvatar(Image image) {
        avatarImage = image;
        avatar = new ImagePattern(image);
    }

    public ImagePattern getAvatar() {
        return avatar;
    }

    public Image getAvatarImage() {
        return avatarImage;
    }

    public void setButtonAvatarVbox(Button button, VBox vBox) {
        this.button = button;
        this.vBox = vBox;
    }

    @Override
    public int compareTo(User o) {
        return o.getDate().compareTo(this.date);
    }
}
