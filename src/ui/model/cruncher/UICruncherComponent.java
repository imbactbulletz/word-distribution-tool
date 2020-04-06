package ui.model.cruncher;

import app.component.cruncher.CruncherComponent;
import javafx.collections.FXCollections;

import java.util.List;

public class UICruncherComponent {

    private final CruncherComponent cruncherComponent;

    private final String name;

    private final List<String> activeJobNames = FXCollections.observableArrayList();

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

    public List<String> getActiveJobNames() {
        return activeJobNames;
    }
}
