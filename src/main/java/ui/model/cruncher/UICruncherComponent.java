package ui.model.cruncher;

import app.component.cruncher.CruncherComponent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class UICruncherComponent {

    private final CruncherComponent cruncherComponent;

    private final String name;

    private final ObservableList<String> activeJobNames = FXCollections.observableArrayList();

    public UICruncherComponent(CruncherComponent cruncherComponent, String name) {
        this.cruncherComponent = cruncherComponent;
        this.name = name;
    }

    public CruncherComponent getCruncherComponent() {
        return cruncherComponent;
    }

    public String getName() {
        return name;
    }

    public ObservableList<String> getActiveJobNames() {
        return activeJobNames;
    }
}
