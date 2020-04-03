package ui.controller;

import app.component.cruncher.CounterCruncher;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.model.cruncher.CruncherControllerModel;
import ui.model.cruncher.UICruncherComponent;
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

    private Consumer<UICruncherComponent> onTableItemSelectedListener;

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
                    String counterCruncherName = "Counter " + model.getTotalCrunchersCreated();
                    model.getCruncherComponents().add(new UICruncherComponent(counterCruncher, counterCruncherName));

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
                model.getCruncherComponents().remove(selectedItem);

                if (model.getCruncherComponents().size() == 0) {
                    removeCruncherButton.setDisable(true);
                    // notify main controller cruncher table is empty
                    onTableItemSelectedListener.accept(null);
                }
            }
        });
    }

    private void initLinkUnlinkButton() {
        linkUnlinkCruncherButton.setDisable(true);
    }

    public void setLinkUnlinkButtonStateAndText(boolean isDisabled, boolean isLinked) {
        linkUnlinkCruncherButton.setDisable(isDisabled);

        String buttonText;
        if(isLinked) {
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

    public void setOnTableItemSelectedListener(Consumer<UICruncherComponent> onTableItemSelectedListener) {
        this.onTableItemSelectedListener = onTableItemSelectedListener;
    }

    public UICruncherComponent getSelectedCruncherComponent() {
        return crunchersTableView.getSelectionModel().getSelectedItem();
    }
}
