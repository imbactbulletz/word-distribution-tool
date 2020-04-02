package ui.model;

import app.component.input.FileInput;
import app.component.input.InputComponent;
import app.component.input.InputComponentState;

public class UIInputComponent {

    private final InputComponent inputComponent;

    private String statusMessage;

    private InputComponentState state;

    public UIInputComponent(InputComponent inputComponent) {
        this.inputComponent = inputComponent;
    }

    public String getName() {
        String name = null;
        if (inputComponent instanceof FileInput) {
            name = "[FI] " + ((FileInput) inputComponent).getDiskPath();
        }
        return name;
    }

    public InputComponent getInputComponent() {
        return inputComponent;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public InputComponentState getState() {
        return state;
    }

    public void setState(InputComponentState state) {
        this.state = state;
    }
}
