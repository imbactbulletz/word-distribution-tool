package ui.model.cruncher;

import app.component.cruncher.CruncherComponent;

public class UICruncherModel {

    private final CruncherComponent cruncherComponent;

    private final String name;

    public UICruncherModel(CruncherComponent cruncherComponent, String name) {
        this.cruncherComponent = cruncherComponent;
        this.name = name;
    }

    public CruncherComponent getCruncherComponent() {
        return cruncherComponent;
    }

    public String getName() {
        return name;
    }
}
