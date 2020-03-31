package ui.controller;

import app.component.input.FileInput;
import app.component.input.InputComponent;
import app.global.Config;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.model.InputControllerModel;
import ui.model.UIInputComponent;
import ui.util.DialogUtil;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InputController {
    private final InputControllerModel model = new InputControllerModel();
    private final ExecutorService componentExecutorService = Executors.newCachedThreadPool();
    @FXML
    private Button addInputButton;
    @FXML
    private Button removeInputButton;
    @FXML
    private Button startPauseInputButton;
    @FXML
    private TableView<UIInputComponent> inputTableView;

    public void init() {
        initAddInputButton();
        initRemoveInputButton();
        initStartPauseInputButton();
        initTableView();
    }

    private void initAddInputButton() {
        addInputButton.setOnAction((e) -> {
            Optional<String> result = DialogUtil.showChoiceDialog(Arrays.asList(Config.DISK_NAMES), "Add File Input", "Choose a disk:");
            result.ifPresent(this::addFileInput);
        });
    }

    public void initRemoveInputButton() {
        // disable until a file input is added
        removeInputButton.setDisable(true);

        removeInputButton.setOnAction((e) -> {
            UIInputComponent selectedUiInputComponent = inputTableView.getSelectionModel().getSelectedItem();

            selectedUiInputComponent.getInputComponent().shutdown();
        });
    }

    private void initStartPauseInputButton() {
        startPauseInputButton.setOnAction((e) -> {
            UIInputComponent selectedItem = inputTableView.getSelectionModel().getSelectedItem();

            if (selectedItem.getStatus() == null || selectedItem.getStatus().equals("Paused")) {
                selectedItem.getInputComponent().resume();
            } else {
                selectedItem.getInputComponent().pause();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initTableView() {
        TableColumn<UIInputComponent, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().getName()));

        TableColumn<UIInputComponent, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory((cellData) -> {
            if (cellData.getValue().getStatus() == null) {
                return new SimpleStringProperty("Idle");
            } else {
                return new SimpleStringProperty(cellData.getValue().getStatus());
            }
        });

        inputTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                setStartPauseInputButtonTextByStatus(newSelection.getStatus());
            }
        });

        inputTableView.getColumns().addAll(nameColumn, statusColumn);
        inputTableView.setItems(model.getUiInputComponents());
    }

    private void setStartPauseInputButtonTextByStatus(String status) {
        String text;
        if (status == null || status.equals("Paused")) {
            text = "Start";
        } else {
            text = "Pause";
        }
        startPauseInputButton.setText(text);
    }

    private void addFileInput(String diskPath) {
        boolean suchFileInputAlreadyExists = model.getUiInputComponents().stream().anyMatch((uiInputComponent -> uiInputComponent.getName().contains(diskPath)));

        if (suchFileInputAlreadyExists) {
            DialogUtil.showErrorDialog("Add File Input", "File input for disk " + diskPath + "already exists!");
            return;
        }

        FileInput fileInput = new FileInput(diskPath);
        UIInputComponent uiInputComponent = new UIInputComponent(fileInput);
        model.getUiInputComponents().add(uiInputComponent);
        inputTableView.getSelectionModel().select(uiInputComponent);
        componentExecutorService.submit(fileInput);
        removeInputButton.setDisable(false);
    }

    public void refreshEntry(InputComponent inputComponent, String statusMessage) {
        Optional<UIInputComponent> uiInputComponentOptional = model.getUiInputComponents().stream()
                .filter((uiInputComponent1 -> uiInputComponent1.getInputComponent() == inputComponent)).findFirst();

        uiInputComponentOptional.ifPresent((uiInputComponent) -> {
            uiInputComponent.setStatus(statusMessage);

            if (inputTableView.getSelectionModel().getSelectedItem() == uiInputComponent) {
                setStartPauseInputButtonTextByStatus(statusMessage);
            }

            inputTableView.refresh();
        });
    }

    public void removeComponent(InputComponent inputComponent) {
        Optional<UIInputComponent> uiInputComponentOptional = model.getUiInputComponents().stream()
                .filter((uiInputComponent1 -> uiInputComponent1.getInputComponent() == inputComponent)).findFirst();

        uiInputComponentOptional.ifPresent((uiInputComponent -> {
            model.getUiInputComponents().remove(uiInputComponent);

            // disable if no more items are present in the table model
            if (model.getUiInputComponents().size() == 0) {
                removeInputButton.setDisable(true);
            }
        }));
    }
}
