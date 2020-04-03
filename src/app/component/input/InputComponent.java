package app.component.input;

public interface InputComponent {

    void pause();

    void resume();

    void shutdown();

    InputComponentState getState();
}
