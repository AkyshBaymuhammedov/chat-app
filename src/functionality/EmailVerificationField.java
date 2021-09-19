package functionality;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import validator.EmailValidator;

/**
 * Created by Akysh on 7/27/2017.
 */
public class EmailVerificationField extends TextField {

    private XMPPManager xmppManager;
    private EmailValidator validator;

    public EmailVerificationField(XMPPManager xmppManager, EmailValidator validator) {
        super();
        this.xmppManager = xmppManager;
        this.validator = validator;

        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (oldValue) {
                    if (getText().length() > 0) {
                        if (validator.validate(getText())) {
                            try {
                                boolean exist = xmppManager.searchIfExits(getText(),0,"email");
                                if (exist) {
                                    setStyle("-fx-background-color: indianred");
                                    setPromptText("email is not available");
                                } else {
                                    setStyle("");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            setStyle("-fx-background-color: indianred");
                            setPromptText("invalid email format");
                        }
                    }
                }
            }
        });
    }

}
