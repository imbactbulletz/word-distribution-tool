package ui.controller;

import app.component.output.OutputCache;
import app.component.output.result.OutputResult;
import app.global.Executors;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import ui.model.output.UIOutputComponent;

import java.util.List;

public class OutputController {
    public static final UIOutputComponent UI_OUTPUT_COMPONENT;

    static {
        OutputCache outputCache = new OutputCache();
        UI_OUTPUT_COMPONENT = new UIOutputComponent(outputCache);
        Executors.COMPONENT.submit(outputCache);
    }

    @FXML
    private LineChart<Long, Long> chartView;
    @FXML
    private ListView<OutputResult> resultsListView;
    @FXML
    private Button singleResultButton;
    @FXML
    private Button sumResultButton;


    public static void updateOutputResult(OutputResult outputResult) {
        List<OutputResult> outputResults = UI_OUTPUT_COMPONENT.getOutputResults();
        int indexOfOutputResult = outputResults.indexOf(outputResult);
        if (indexOfOutputResult != -1) {
           outputResults.set(indexOfOutputResult, outputResult);
        } else {
            outputResults.add(outputResult);
        }
    }

    public void init() {
        initResultsListView();
    }

    private void initResultsListView() {
        resultsListView.setItems(UI_OUTPUT_COMPONENT.getOutputResults());
        resultsListView.setCellFactory(callback -> new ListCell<>() {
            @Override
            protected void updateItem(OutputResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    boolean isComplete = item.getProgress() == 100;
                    setText(isComplete ? item.getResultName() : "*" + item.getResultName());
                }
            }
        });
    }
}
