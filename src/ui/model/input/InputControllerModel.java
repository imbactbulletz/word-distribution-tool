package ui.model.input;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InputControllerModel {

    private ObservableList<UIInputComponent> uiInputComponents = FXCollections.observableArrayList();
    public ObservableList<UIInputComponent> getUiInputComponents() {
        return uiInputComponents;
    }
}
