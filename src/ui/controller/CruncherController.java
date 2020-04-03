package ui.controller;

import app.component.cruncher.CounterCruncher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ui.model.cruncher.CruncherControllerModel;
import ui.model.cruncher.UICruncherModel;
import ui.util.DialogUtil;

import java.util.Optional;


public class CruncherController {

    private final CruncherControllerModel model = new CruncherControllerModel();

    @FXML
    private Button addCruncherButton;


    public void init() {
        initAddCruncherButton();
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
}
