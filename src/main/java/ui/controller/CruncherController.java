package ui.controller;

import app.component.cruncher.CounterCruncher;
import app.component.cruncher.CruncherComponent;
import app.component.cruncher.CruncherJobStatus;
import app.component.input.FileInfo;
import app.component.input.FileInfoPoison;
import app.component.input.InputComponent;
import app.global.Executors;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.model.cruncher.CruncherControllerModel;
import ui.model.cruncher.UICruncherComponent;
import ui.model.input.UIInputComponent;
import ui.util.DialogUtil;

import java.util.Optional;
import java.util.function.Consumer;


public class CruncherController {

    private final CruncherControllerModel model = new CruncherControllerModel();
    @FXML
    private Button addCruncherButton;
    @FXML
    private Button removeCruncherButton;
    @FXML
    private Button linkUnlinkCruncherButton;
    @FXML
    private TableView<UICruncherComponent> crunchersTableView;
    @FXML
    private ListView<String> statusesListView;

    private Consumer<UICruncherComponent> onTableItemSelectedListener;

    private Consumer<UICruncherComponent> onLinkUnlinkButtonClickedListener;

    public void init() {
        initAddCruncherButton();
        initRemoveCruncherButton();
        initLinkUnlinkButton();
        initCrunchersTableView();
    }

    private void initAddCruncherButton() {
        addCruncherButton.setOnAction((e) -> {
            Optional<String> result = DialogUtil.showTextInputDialog("Add Cruncher", "Enter arity", "1");
            result.ifPresent((arityStringValue) -> {
                try {
                    int arityValue = Integer.parseInt(arityStringValue);
                    model.incrementTotalCrunchersCreated();
                    CounterCruncher counterCruncher = new CounterCruncher(arityValue);
                    counterCruncher.addOutputComponent(OutputController.UI_OUTPUT_COMPONENT.getOutputComponent());
                    String counterCruncherName = "Counter " + model.getTotalCrunchersCreated();
                    model.getCruncherComponents().add(new UICruncherComponent(counterCruncher, counterCruncherName));
                    Executors.COMPONENT.submit(counterCruncher);
                    crunchersTableView.getSelectionModel().selectLast();

                    if (removeCruncherButton.isDisable()) {
                        removeCruncherButton.setDisable(false);
                    }
                } catch (NumberFormatException nex) {
                    DialogUtil.showErrorDialog("Add Cruncher", "Invalid arity. Cruncher not added.");
                }
            });
        });
    }

    private void initRemoveCruncherButton() {
        // initially disabled, since no content is present
        removeCruncherButton.setDisable(true);

        removeCruncherButton.setOnAction((e) -> {
            UICruncherComponent selectedItem = crunchersTableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                for (UIInputComponent uiInputComponent : selectedItem.getLinkedUiInputComponents()) {
                    uiInputComponent.getInputComponent().getCruncherComponents().remove(selectedItem.getCruncherComponent());
                }

                model.getCruncherComponents().remove(selectedItem);
                if (model.getCruncherComponents().size() == 0) {
                    removeCruncherButton.setDisable(true);
                    // notify main controller cruncher table is empty
                    if (onTableItemSelectedListener != null) {
                        onTableItemSelectedListener.accept(null);
                    } else {
                        System.err.println("Table item selected listener must not be null.");
                    }
                }
            }
        });
    }

    private void initLinkUnlinkButton() {
        linkUnlinkCruncherButton.setDisable(true);
        linkUnlinkCruncherButton.setOnAction((e) -> {
            UICruncherComponent uiCruncherComponent = crunchersTableView.getSelectionModel().getSelectedItem();

            if (onLinkUnlinkButtonClickedListener != null) {
                onLinkUnlinkButtonClickedListener.accept(uiCruncherComponent);
            } else {
                System.err.println("Link/Unlink button click listener must not be null.");
            }
        });
    }

    public void setLinkUnlinkButtonStateAndText(boolean isDisabled, boolean isLinked) {
        linkUnlinkCruncherButton.setDisable(isDisabled);

        String buttonText;
        if (isLinked) {
            buttonText = "Unlink";
        } else {
            buttonText = "Link";
        }
        linkUnlinkCruncherButton.setText(buttonText);
    }

    private void initCrunchersTableView() {
        createColumns();
        crunchersTableView.setItems(model.getCruncherComponents());

        crunchersTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                if (onTableItemSelectedListener != null) {
                    onTableItemSelectedListener.accept(newSelection);
                    statusesListView.setItems(newSelection.getActiveJobNames());
                } else {
                    System.err.println("Table item listener must not be null.");
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void createColumns() {
        TableColumn<UICruncherComponent, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().getName()));
        TableColumn<UICruncherComponent, Integer> arityColumn = new TableColumn<>("Arity");
        arityColumn.setCellValueFactory((cellData) -> new SimpleObjectProperty<>(cellData.getValue().getCruncherComponent().getArity()));
        crunchersTableView.getColumns().addAll(nameColumn, arityColumn);
    }

    public void refreshJobStatus(CruncherComponent cruncherComponent, String jobName, CruncherJobStatus cruncherJobStatus) {
        Optional<UICruncherComponent> uiCruncherComponentOptional = model.getCruncherComponents().stream()
                .filter((uiCruncherComponent -> uiCruncherComponent.getCruncherComponent() == cruncherComponent)).findFirst();

        uiCruncherComponentOptional.ifPresent((uiCruncherComponent -> {
            if (cruncherJobStatus == CruncherJobStatus.IS_CRUNCHING) {
                uiCruncherComponent.getActiveJobNames().add(jobName);
            } else if (cruncherJobStatus == CruncherJobStatus.IS_DONE) {
                uiCruncherComponent.getActiveJobNames().remove(jobName);
            }
        }));
    }

    public UICruncherComponent getSelectedCruncherComponent() {
        return crunchersTableView.getSelectionModel().getSelectedItem();
    }

    public void setOnTableItemSelectedListener(Consumer<UICruncherComponent> onTableItemSelectedListener) {
        this.onTableItemSelectedListener = onTableItemSelectedListener;
    }

    public void setOnLinkUnlinkButtonClickedListener(Consumer<UICruncherComponent> onLinkUnlinkButtonClickedListener) {
        this.onLinkUnlinkButtonClickedListener = onLinkUnlinkButtonClickedListener;
    }

    public boolean hasLinkedCrunchers() {
        for(UICruncherComponent uiCruncherComponent: model.getCruncherComponents()) {
            if (uiCruncherComponent.getLinkedUiInputComponents().size() != 0) return true;
        }

        return false;
    }

    public boolean hasActiveCrunchers() {
        return model.getCruncherComponents().size() != 0;
    }

    public void shutdownUnlinkedCrunchers() {
        FileInfoPoison poison = new FileInfoPoison(null, null, null);
        for(UICruncherComponent uiCruncherComponent: model.getCruncherComponents()) {
            if(uiCruncherComponent.getLinkedUiInputComponents().size() == 0) {
                uiCruncherComponent.getCruncherComponent().queueWork(poison);
            }
        }
    }
}
