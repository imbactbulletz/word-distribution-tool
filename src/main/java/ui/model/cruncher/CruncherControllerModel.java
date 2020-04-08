package ui.model.cruncher;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CruncherControllerModel {

    private ObservableList<UICruncherComponent> cruncherComponents = FXCollections.observableArrayList();

    private int totalCrunchersCreated = 0;

    public ObservableList<UICruncherComponent> getCruncherComponents() {
        return cruncherComponents;
    }

    public void incrementTotalCrunchersCreated() {
        totalCrunchersCreated++;
    }

    public int getTotalCrunchersCreated() {
        return totalCrunchersCreated;
    }
}
