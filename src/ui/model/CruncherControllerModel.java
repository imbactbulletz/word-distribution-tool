package ui.model;

import app.component.cruncher.CruncherComponent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CruncherControllerModel {

    private ObservableList<CruncherComponent> cruncherComponents = FXCollections.observableArrayList();

    public ObservableList<CruncherComponent> getCruncherComponents() {
        return cruncherComponents;
    }
}
