package ui.controller;

import app.component.cruncher.CruncherComponent;
import app.component.input.InputComponent;
import app.global.Executors;
import javafx.application.Platform;
import ui.model.cruncher.UICruncherComponent;
import ui.model.input.UIInputComponent;
import ui.util.DialogUtil;

import java.util.concurrent.TimeUnit;


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

            if (inputComponent.getCruncherComponents().contains(cruncherComponent)) {
                inputComponent.getCruncherComponents().remove(cruncherComponent);
                cruncherComponent.getLinkedInputComponents().remove(inputComponent);
            } else {
                inputComponent.getCruncherComponents().add(cruncherComponent);
                cruncherComponent.getLinkedInputComponents().add(inputComponent);
            }

            setLinkUnlinkButtonStateAndText(uiInputComponent, uiCruncherComponent);
        }));
    }

    public static void showOutOfMemoryErrorDialog() {
        DialogUtil.showErrorDialog("Out of Memory", "Application has ran out of memory. It will shutdown now.");
        System.exit(-1);
    }

    public static void terminateEverything() {
        DialogUtil.showModalInfoDialog("Shutting down", "Application is shutting down.");
        INPUT_CONTROLLER.terminateFileInputComponents();

        Thread shutdownThread = new Thread(() -> {
           Executors.COMPONENT.shutdown();
           while (!Executors.COMPONENT.isTerminated()) {
               try {
                   Executors.COMPONENT.awaitTermination(5, TimeUnit.SECONDS);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }

           Executors.INPUT.shutdown();
           while(!Executors.INPUT.isTerminated()) {
               try {
                   Executors.INPUT.awaitTermination(5, TimeUnit.SECONDS);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }

           Executors.CRUNCHER.shutdown();
           while(!Executors.CRUNCHER.isTerminated()) {
               try {
                   Executors.CRUNCHER.awaitTermination(5, TimeUnit.SECONDS);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }

            Platform.exit();
        });
        shutdownThread.start();
    }

    private void setLinkUnlinkButtonStateAndText(UIInputComponent uiInputComponent, UICruncherComponent uiCruncherComponent) {
        if (uiInputComponent == null || uiCruncherComponent == null) {
            CRUNCHER_CONTROLLER.setLinkUnlinkButtonStateAndText(true, false);
            return;
        }

        if (uiInputComponent.getInputComponent().getCruncherComponents().contains(uiCruncherComponent.getCruncherComponent())) {
            CRUNCHER_CONTROLLER.setLinkUnlinkButtonStateAndText(false, true);
        } else {
            CRUNCHER_CONTROLLER.setLinkUnlinkButtonStateAndText(false, false);
        }
    }
}
