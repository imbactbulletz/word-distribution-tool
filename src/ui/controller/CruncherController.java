package ui.controller;

import app.component.cruncher.CounterCruncher;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.model.cruncher.CruncherControllerModel;
import ui.model.cruncher.UICruncherModel;
import ui.util.DialogUtil;

import java.util.Optional;


public class CruncherController {

    private final CruncherControllerModel model = new CruncherControllerModel();

    @FXML
    private Button addCruncherButton;
    @FXML
    private TableView<UICruncherModel> crunchersTableView;

    public void init() {
        initAddCruncherButton();
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
                    model.getCruncherComponents().add(new UICruncherModel(counterCruncher, counterCruncherName));
                } catch (NumberFormatException nex) {
                    DialogUtil.showErrorDialog("Add Cruncher", "Invalid arity. Cruncher not added.");
                }
            });
        });
    }

    private void initCrunchersTableView() {
        createColumns();
        crunchersTableView.setItems(model.getCruncherComponents());
    }

    @SuppressWarnings("unchecked")
    private void createColumns() {
        TableColumn<UICruncherModel, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory((cellData) -> new SimpleStringProperty(cellData.getValue().getName()));
        TableColumn<UICruncherModel, Integer> arityColumn = new TableColumn<>("Arity");
        arityColumn.setCellValueFactory((cellData) -> new SimpleObjectProperty<>(cellData.getValue().getCruncherComponent().getArity()));
        crunchersTableView.getColumns().addAll(nameColumn, arityColumn);
    }
}
