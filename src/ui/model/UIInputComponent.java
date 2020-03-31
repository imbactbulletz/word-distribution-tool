package ui.model;

import app.component.input.FileInput;
import app.component.input.InputComponent;

public class UIInputComponent {

    private final InputComponent inputComponent;

    private String status = "Idle";

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
