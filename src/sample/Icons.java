package sample;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;


public class Icons extends Label{

    private Node icon;

    private AnimationType type;

    public Icons(String text, Node icon, AnimationType type) {
        setText(text);
        setGraphic(icon);
        this.icon = icon;
        this.type = type;
        addAnimation();
    }

    private void addBlinkAnimation() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        final KeyValue kv = new KeyValue(icon.opacityProperty(), 0.0);
        final KeyFrame kf = new KeyFrame(Duration.millis(700), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    private void addJumpAnimation() {
        final TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), icon);
        final double start = 0.0;
        final double end = start - 4.0;
        translateTransition.setFromY(start);
        translateTransition.setToY(end);
        translateTransition.setCycleCount(-1);
        translateTransition.setAutoReverse(true);
        translateTransition.setInterpolator(Interpolator.EASE_BOTH);
        translateTransition.play();
    }

    public enum AnimationType {

        BLINK, JUMP, NONE;
    }

    ;

    private void addAnimation() {
        switch (type) {
            case BLINK:
                addBlinkAnimation();
                break;
            case JUMP:
                addJumpAnimation();
                break;
            case NONE:
                break;
            default:
                break;
        }
    }

    public void setIcon(Node icon){
        this.icon=icon;
    }
}