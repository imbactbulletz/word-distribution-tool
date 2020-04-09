package ui.controller;

import app.component.cruncher.CruncherComponent;
import app.component.input.InputComponent;
import ui.model.cruncher.UICruncherComponent;
import ui.model.input.UIInputComponent;
import ui.util.DialogUtil;

public class MainController {

    public static final InputController INPUT_CONTROLLER = new InputController();
    public static final CruncherController CRUNCHER_CONTROLLER = new CruncherController();

    public MainController() {
        CRUNCHER_CONTROLLER.setOnTableItemSelectedListener((uiCruncherComponent -> {
            UIInputComponent uiInputComponent = INPUT_CONTROLLER.getSelectedInputComponent();
            setLinkUnlinkButtonStateAndText(uiInputComponent, uiCruncherComponent);
        }));

        INPUT_CONTROLLER.setOnInputTableItemSelectedListener(uiInputComponent -> {
            UICruncherComponent uiCruncherComponent = CRUNCHER_CONTROLLER.getSelectedCruncherComponent();
            setLinkUnlinkButtonStateAndText(uiInputComponent, uiCruncherComponent);
        });

        CRUNCHER_CONTROLLER.setOnLinkUnlinkButtonClickedListener((uiCruncherComponent -> {
            CruncherComponent cruncherComponent = uiCruncherComponent.getCruncherComponent();
            UIInputComponent uiInputComponent = INPUT_CONTROLLER.getSelectedInputComponent();
            InputComponent inputComponent = uiInputComponent.getInputComponent();

            if(inputComponent.getCruncherComponents().contains(cruncherComponent)) {
                inputComponent.getCruncherComponents().remove(cruncherComponent);
                cruncherComponent.getLinkedInputComponents().remove(inputComponent);
            } else {
                inputComponent.getCruncherComponents().add(cruncherComponent);
                cruncherComponent.getLinkedInputComponents().add(inputComponent);
            }

            setLinkUnlinkButtonStateAndText(uiInputComponent, uiCruncherComponent);
        }));
    }

    private void setLinkUnlinkButtonStateAndText(UIInputComponent uiInputComponent, UICruncherComponent uiCruncherComponent) {
        if(uiInputComponent == null || uiCruncherComponent == null) {
            CRUNCHER_CONTROLLER.setLinkUnlinkButtonStateAndText(true, false);
            return;
        }

        if(uiInputComponent.getInputComponent().getCruncherComponents().contains(uiCruncherComponent.getCruncherComponent())) {
            CRUNCHER_CONTROLLER.setLinkUnlinkButtonStateAndText(false, true);
        } else {
            CRUNCHER_CONTROLLER.setLinkUnlinkButtonStateAndText(false, false);
        }
    }

    public static void showOutOfMemoryErrorDialog() {
        DialogUtil.showErrorDialog("Out of Memory", "Application has ran out of memory. It will shutdown now.");
        System.exit(0);
    }
}
