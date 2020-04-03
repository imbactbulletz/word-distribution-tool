package ui.model.cruncher;

import app.component.cruncher.CruncherComponent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CruncherControllerModel {

    private ObservableList<CruncherComponent> cruncherComponents = FXCollections.observableArrayList();

    private int totalCrunchersCreated = 0;

    public ObservableList<CruncherComponent> getCruncherComponents() {
        return cruncherComponents;
    }

    public void incrementTotalCrunchersCreated() {
        totalCrunchersCreated++;
    }

    public int getTotalCrunchersCreated() {
        return totalCrunchersCreated;
    }
}
