package ui.controller;

import app.component.input.FileInput;
import app.component.input.InputComponent;
import app.component.input.InputComponentState;
import app.global.Config;
import app.global.Executors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ui.model.input.InputControllerModel;
import ui.model.input.UIInputComponent;
import ui.util.DialogUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class InputController {
    private final InputControllerModel model = new InputControllerModel();
    @FXML
    private Button addInputButton;
    @FXML
    private Button removeInputButton;
    @FXML
    private Button startPauseInputButton;
    @FXML
    private TableView<UIInputComponent> inputTableView;

    @FXML
    private Button addDirectoryButton;
    @FXML
    private Button removeDirectoryButton;
    @FXML
    private ListView<File> directoriesListView;

    private Consumer<UIInputComponent> onInputTableItemSelectedListener;

    public void init() {
        // file input controls
        initAddInputButton();
        initRemoveInputButton();
        initStartPauseInputButton();
        initTableView();

        // directory controls
        initAddDirectoryButton();
        initRemoveDirectoryButton();
        initDirectoriesListView();
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
        startPauseInputButton.setDisable(true);
        startPauseInputButton.setOnAction((e) -> {
            UIInputComponent selectedItem = inputTableView.getSelectionModel().getSelectedItem();

            if (selectedItem.getInputComponent().getState() == InputComponentState.NOT_STARTED || selectedItem.getInputComponent().getState() == InputComponentState.PAUSED) {
                selectedItem.getInputComponent().resume();
            } else if (selectedItem.getInputComponent().getState() == InputComponentState.WORKING) {
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
            if (cellData.getValue().getStatusMessage() == null) {
                return new SimpleStringProperty("Idle");
            } else {
                return new SimpleStringProperty(cellData.getValue().getStatusMessage());
            }
        });

        inputTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                setStartPauseInputButtonTextByState(newSelection.getInputComponent().getState());

                if (newSelection.getInputComponent() instanceof FileInput) {
                    directoriesListView.setItems(FXCollections.observableList(((FileInput) newSelection.getInputComponent()).getDirectories()));

                    if(directoriesListView.getItems().size() > 0) {
                        directoriesListView.getSelectionModel().select(directoriesListView.getItems().size() - 1);
                        directoriesListView.refresh();
                    }

                    if (((FileInput) newSelection.getInputComponent()).getDirectories().size() == 0) {
                        removeDirectoryButton.setDisable(true);
                    } else {
                        removeDirectoryButton.setDisable(false);
                    }
                }

                if (onInputTableItemSelectedListener != null) {
                    onInputTableItemSelectedListener.accept(newSelection);
                } else {
                    System.err.println("Input table selected item listener must not be null.");
                }
            }
        });

        inputTableView.getColumns().addAll(nameColumn, statusColumn);
        inputTableView.setItems(model.getUiInputComponents());
    }

    private void setStartPauseInputButtonTextByState(InputComponentState state) {
        if (state == InputComponentState.NOT_STARTED || state == InputComponentState.PAUSED) {
            startPauseInputButton.setText("Start");
        } else if (state == InputComponentState.WORKING) {
            startPauseInputButton.setText("Pause");
        }
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
        Executors.COMPONENT.submit(fileInput);
        removeInputButton.setDisable(false);
        startPauseInputButton.setDisable(false);
        addDirectoryButton.setDisable(false);
    }

    public void refreshEntryStatus(InputComponent inputComponent, String statusMessage) {
        Optional<UIInputComponent> uiInputComponentOptional = model.getUiInputComponents().stream()
                .filter((uiInputComponent1 -> uiInputComponent1.getInputComponent() == inputComponent)).findFirst();

        uiInputComponentOptional.ifPresent((uiInputComponent) -> {
            uiInputComponent.setStatusMessage(statusMessage);
            inputTableView.refresh();
        });
    }

    public void refreshEntryState(InputComponent inputComponent, InputComponentState inputComponentState) {
        Optional<UIInputComponent> uiInputComponentOptional = model.getUiInputComponents().stream()
                .filter((uiInputComponent1 -> uiInputComponent1.getInputComponent() == inputComponent)).findFirst();

        uiInputComponentOptional.ifPresent((uiInputComponent) -> {
            if (inputTableView.getSelectionModel().getSelectedItem() == uiInputComponent) {
                setStartPauseInputButtonTextByState(inputComponentState);
            }
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
                startPauseInputButton.setDisable(true);
                addDirectoryButton.setDisable(true);
                removeDirectoryButton.setDisable(true);
                directoriesListView.setItems(null);
                // notify main controller that input table is empty
                if (onInputTableItemSelectedListener != null) {
                    onInputTableItemSelectedListener.accept(null);
                } else {
                    System.err.println("Input table selected item listener must not be null.");
                }
            }
        }));
    }

    ////////////////////// DIRECTORY CONTROLS

    private void initAddDirectoryButton() {
        if (model.getUiInputComponents().size() == 0) {
            addDirectoryButton.setDisable(true);
        }

        addDirectoryButton.setOnAction((e) -> {
            InputComponent selectedComponent = inputTableView.getSelectionModel().getSelectedItem().getInputComponent();

            if (selectedComponent instanceof FileInput) {
                String diskPath = ((FileInput) selectedComponent).getDiskPath();
                String absoluteDiskPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + diskPath;
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

                File directory = DialogUtil.showDirectoryChooser(absoluteDiskPath, stage);
                if(directory == null) {
                    return;
                }

                ((FileInput) selectedComponent).addDirectory(directory);
                directoriesListView.setItems(FXCollections.observableList(((FileInput) selectedComponent).getDirectories()));
                // select first item since listView doesn't select it automatically
                directoriesListView.getSelectionModel().select(0);
                removeDirectoryButton.setDisable(false);
            }
        });
    }

    private void initRemoveDirectoryButton() {
        removeDirectoryButton.setDisable(true);
        removeDirectoryButton.setOnAction((e) -> {
            UIInputComponent selectedUIInputComponent = inputTableView.getSelectionModel().getSelectedItem();
            File selectedDirectory = directoriesListView.getSelectionModel().getSelectedItem();

            if (selectedUIInputComponent != null && selectedDirectory != null && selectedUIInputComponent.getInputComponent() instanceof FileInput) {
                List<File> directoriesList = ((FileInput) selectedUIInputComponent.getInputComponent()).getDirectories();
                int selectedDirectoryIndex = directoriesList.indexOf(selectedDirectory);
                if (directoriesList.size() > 1) {
                    if (selectedDirectoryIndex == 0) {
                        directoriesListView.getSelectionModel().select(1);
                    } else {
                        directoriesListView.getSelectionModel().select(selectedDirectoryIndex - 1);
                    }
                } else {
                    removeDirectoryButton.setDisable(true);
                }

                ((FileInput) selectedUIInputComponent.getInputComponent()).removeDirectory(selectedDirectory);
                directoriesListView.refresh();
            }
        });
    }

    private void initDirectoriesListView() {
        directoriesListView.setCellFactory(callback -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    public UIInputComponent getSelectedInputComponent() {
        return inputTableView.getSelectionModel().getSelectedItem();
    }

    public void setOnInputTableItemSelectedListener(Consumer<UIInputComponent> onInputTableItemSelectedListener) {
        this.onInputTableItemSelectedListener = onInputTableItemSelectedListener;
    }
}
