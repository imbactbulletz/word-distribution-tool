package ui.model.cruncher;

import app.component.cruncher.CruncherComponent;

public class UICruncherComponent {

    private final CruncherComponent cruncherComponent;

    private final String name;

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
}
