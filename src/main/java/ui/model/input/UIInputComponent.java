package ui.model.input;

import app.component.input.FileInput;
import app.component.input.InputComponent;

public class UIInputComponent {

    private final InputComponent inputComponent;

    private String statusMessage;

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
}
