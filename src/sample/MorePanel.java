package sample;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Created by Akysh on 7/2/2017.
 */
public class MorePanel {
    private Stage mainStage;
    private BorderPane mainLayout;
    private HBox upperBox;

    public MorePanel(BorderPane mainLayout) {
        this.mainLayout=mainLayout;
        createMorePanel();
    }

    public void createMorePanel(){
        Button settings = new Button("Settings");
        Button profile = new Button("Profile");
        upperBox = new HBox(settings,profile);
        upperBox.setSpacing(5);
        upperBox.setAlignment(Pos.CENTER);
    }

    public void showMorePanel(){
        mainLayout.setTop(upperBox);
        mainLayout.setCenter(null);
    }
}
