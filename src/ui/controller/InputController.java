package ui.controller;

import app.component.input.FileInput;
import app.global.Config;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private TableView<UIInputComponent> inputTableView;

    public void init() {
        initAddInputButton();
        initRemoveInputButton();
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
            model.getUiInputComponents().remove(selectedUiInputComponent);

            // disable if no more items are present in the table model
            if(model.getUiInputComponents().size() == 0) {
                removeInputButton.setDisable(true);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initTableView() {
        TableColumn<UIInputComponent, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().getName()));

        TableColumn<UIInputComponent, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        inputTableView.getColumns().addAll(nameColumn, statusColumn);
        inputTableView.setItems(model.getUiInputComponents());
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
}
