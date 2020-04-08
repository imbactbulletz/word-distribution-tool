package app.component.input;

import app.component.cruncher.CruncherComponent;

import java.util.List;

public interface InputComponent {

    void pause();

    void resume();

    void shutdown();

    InputComponentState getState();

    List<CruncherComponent> getCruncherComponents();
}
