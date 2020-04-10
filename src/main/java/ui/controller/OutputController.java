package ui.controller;

import app.component.cruncher.typealias.BagOfWords;
import app.component.cruncher.typealias.CalculationResult;
import app.component.output.OutputCache;
import app.component.output.OutputComponentSumRequest;
import app.component.output.result.OutputResult;
import app.global.Config;
import app.global.Executors;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.util.Duration;
import ui.model.output.UIOutputComponent;
import ui.util.DialogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static javafx.scene.chart.XYChart.*;

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
    @FXML
    private ProgressBar progressBar;

    public static void updateOutputResult(OutputResult outputResult) {
        List<OutputResult> outputResults = UI_OUTPUT_COMPONENT.getOutputResults();
        int indexOfOutputResult = outputResults.indexOf(outputResult);
        if (indexOfOutputResult != -1) {
            outputResults.set(indexOfOutputResult, outputResult);
        } else {
            outputResults.add(outputResult);
        }
    }

    public void updateProgressBar(double value) {
        progressBar.setVisible(true);
        progressBar.setProgress(value);
        progressBar.requestLayout();
        if(value == 1) {
            progressBar.setVisible(false);
        }
    }

    public void init() {
        initSingleResultButton();
        initSumResultButton();
        initResultsListView();
    }

    private void initResultsListView() {
        resultsListView.setItems(UI_OUTPUT_COMPONENT.getOutputResults());
        resultsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        resultsListView.getItems().addListener((ListChangeListener<? super OutputResult>) (change) -> {
            if (change.getList().size() > 0) {
                singleResultButton.setDisable(false);
                sumResultButton.setDisable(false);
                selectResultListViewItemIfNoneSelected();
            } else {
                sumResultButton.setDisable(true);
                sumResultButton.setDisable(true);
            }
        });

        resultsListView.setCellFactory(callback -> new ListCell<>() {
            @Override
            protected void updateItem(OutputResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    boolean isComplete = item.getProgress() == 1;
                    setText(isComplete ? item.getResultName() : "*" + item.getResultName());
                }
            }
        });
    }

    private void selectResultListViewItemIfNoneSelected() {
        OutputResult outputResult = resultsListView.getSelectionModel().getSelectedItem();
        if (outputResult == null) {
            resultsListView.getSelectionModel().select(resultsListView.getItems().size() - 1);
        }
    }

    private void initSingleResultButton() {
        singleResultButton.setDisable(true);
        singleResultButton.setOnAction((e) -> {
            OutputResult selectedOutputResult = resultsListView.getSelectionModel().getSelectedItem();
            if (selectedOutputResult != null) {
                try {
                    CalculationResult calculationResult = UI_OUTPUT_COMPONENT.getOutputComponent().poll(selectedOutputResult.getResultName());

                    if (calculationResult == null) {
                        DialogUtil.showErrorDialog("Result not ready", "Result is not ready yet.");
                    } else {
                        int duration = (int) ((calculationResult.size() * Math.log(calculationResult.size())) / Config.SORT_PROGRESS_LIMIT_RATE);
                        interpolateProgressBar(duration);
                        Thread sorterThread = new Thread(() -> {
                           List<Map.Entry<BagOfWords, Long>> entries = calculationResult.entrySet().stream().sorted(Map.Entry.comparingByValue((v1, v2) -> {
                               if(v1.equals(v2)) return 0;
                               if(v1 > v2) return -1;
                               return 1;
                           })).collect(Collectors.toList());
                           List<Map.Entry<BagOfWords, Long>> subEntries = entries.subList(0,100);
                           List<Data<Long,Long>> chartData = subEntries.stream().map((entry) -> new XYChart.Data<>((long) subEntries.indexOf(entry), entry.getValue())).collect(Collectors.toList());
                           Series<Long,Long> series = new Series<>();
                           for(Data<Long,Long> data: chartData) {
                               series.getData().add(data);
                           }

                            Platform.runLater(() -> {
                                chartView.getData().clear();
                                chartView.getData().add(series);
                            });
                        });
                        sorterThread.start();
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void initSumResultButton() {
        sumResultButton.setDisable(true);
        sumResultButton.setOnAction((e) -> {
            Optional<String> dialogResult = DialogUtil.showTextInputDialog("Enter sum name", "Enter a unique name for sum", "sum");
            dialogResult.ifPresent((sumName) -> {
                if(sumName.isBlank()) {
                   DialogUtil.showErrorDialog("Invalid name", "You have to enter a name.");
                }

                if (UI_OUTPUT_COMPONENT.getOutputResults().contains(new OutputResult(sumName, 0, null))) {
                    DialogUtil.showErrorDialog("Name not unique", "Name of the sum must be unique.");
                } else {
                    List<OutputResult> outputResults = resultsListView.getSelectionModel().getSelectedItems();
                    if (outputResults != null && outputResults.size() > 0) {
                        List<String> resultNames = new ArrayList<>();
                        for (OutputResult outputResult : outputResults) {
                            resultNames.add(outputResult.getResultName());
                        }

                        UI_OUTPUT_COMPONENT.getOutputComponent().enqueueSumRequest(new OutputComponentSumRequest(sumName, resultNames));
                    }
                }
            });
        });
    }

    private void interpolateProgressBar(int seconds) {
        progressBar.setVisible(true);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(seconds), e-> {
                    // do anything you need here on completion...
                    System.out.println("Minute over");
                }, new KeyValue(progressBar.progressProperty(), 1))
        );
        timeline.play();
    }
}
