package functionality;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import sample.SearchUser;

import java.util.*;
import java.util.stream.Collectors;

public class AutoCompleteTextField extends TextField {
    private final SortedSet<String> entries;
    private ContextMenu entriesPopup;
    private XMPPManager xmppManager;
    private SearchUser searchUser;
    private Map<String, String> users;

    public AutoCompleteTextField(XMPPManager xmppManager, SearchUser searchUser) {
        super();
        this.xmppManager = xmppManager;
        this.searchUser=searchUser;
        entries = new TreeSet<>();
        users = new HashMap<>();
        entriesPopup = new ContextMenu();
        textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                if (getText().length() == 0) {
                    entriesPopup.hide();
                } else {
                    LinkedList<String> searchResult = new LinkedList<>();
                    entries.clear();
                    try {
                        users = xmppManager.searchUser(getText()).stream().collect(Collectors.toMap(e->e.getUser(), e->e.getName()));
                        users.entrySet().stream().forEach(e->entries.add(e.getValue()));
                    } catch (Exception e) {
                        entries.clear();
                        entries.add("No results");
                        e.printStackTrace();
                    }
                    if (entries.size() > 0) {
                        searchResult.addAll(entries);
                        populatePopup(searchResult);
                        if (!entriesPopup.isShowing()) {
                            entriesPopup.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
                        }
                    } else {
                        entriesPopup.hide();
                    }
                }
            }
        });

        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                entriesPopup.hide();
            }
        });

    }

    public SortedSet<String> getEntries() {
        return entries;
    }

    private void populatePopup(List<String> searchResult) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        int maxEntries = 100;
        int count = Math.min(searchResult.size(), maxEntries);
        for (int i = 0; i < count; i++) {
            final String result = searchResult.get(i);
            Label entryLabel = new Label(result);
            entryLabel.setPrefWidth(330);
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    setText(result);
                    entriesPopup.hide();
                    users.entrySet().stream().forEach(e->{
                        if(e.getValue().equals(result)){
                            searchUser.addFoundUser(e.getKey());
                        }
                    });

                }
            });
            menuItems.add(item);
        }
        entriesPopup.getItems().clear();
        entriesPopup.getItems().addAll(menuItems);

    }
}