package ui.model.output;

import app.component.output.OutputComponent;
import app.component.output.result.OutputResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UIOutputComponent {

    private final OutputComponent outputComponent;

    private final ObservableList<OutputResult> outputResults = FXCollections.observableArrayList();

    public UIOutputComponent(OutputComponent outputComponent) {
        this.outputComponent = outputComponent;
    }

    public OutputComponent getOutputComponent() {
        return outputComponent;
    }

    public ObservableList<OutputResult> getOutputResults() {
        return outputResults;
    }
}
