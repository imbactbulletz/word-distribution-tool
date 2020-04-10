package ui.controller;

import app.component.cruncher.CruncherComponent;
import app.component.cruncher.typealias.CruncherResultPoison;
import app.component.input.InputComponent;
import app.global.Executors;
import javafx.application.Platform;
import ui.model.cruncher.UICruncherComponent;
import ui.model.input.UIInputComponent;
import ui.util.DialogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


public class MainController {

    public static final InputController INPUT_CONTROLLER = new InputController();
    public static final CruncherController CRUNCHER_CONTROLLER = new CruncherController();
    public static final OutputController OUTPUT_CONTROLLER = new OutputController();

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
                uiCruncherComponent.getLinkedUiInputComponents().remove(uiInputComponent);
            } else {
                inputComponent.getCruncherComponents().add(cruncherComponent);
                uiCruncherComponent.getLinkedUiInputComponents().add(uiInputComponent);
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
            if((!INPUT_CONTROLLER.hasActiveInputs() && !CRUNCHER_CONTROLLER.hasActiveCrunchers()) || (INPUT_CONTROLLER.hasActiveInputs() && !CRUNCHER_CONTROLLER.hasLinkedCrunchers())) {
                OutputController.UI_OUTPUT_COMPONENT.getOutputComponent().addCruncherResult(new CruncherResultPoison());
                shutdownPoolAndWait(Executors.OUTPUT, 5, TimeUnit.SECONDS);
                CRUNCHER_CONTROLLER.shutdownUnlinkedCrunchers();
                shutdownPoolAndWait(Executors.CRUNCHER, 5, TimeUnit.SECONDS);
            } else if (!CRUNCHER_CONTROLLER.hasLinkedCrunchers()) {
                CRUNCHER_CONTROLLER.shutdownUnlinkedCrunchers();
                shutdownPoolAndWait(Executors.CRUNCHER, 5, TimeUnit.SECONDS);
            }

           shutdownPoolAndWait(Executors.COMPONENT, 5, TimeUnit.SECONDS);
           shutdownPoolAndWait(Executors.INPUT, 5, TimeUnit.SECONDS);
           shutdownPoolAndWait(Executors.CRUNCHER, 5, TimeUnit.SECONDS);
           shutdownPoolAndWait(Executors.OUTPUT, 5, TimeUnit.SECONDS);
           Platform.exit();
        });
        shutdownThread.start();
    }

    private static  void shutdownPoolAndWait(ExecutorService threadPoolExecutor, int timeOut, TimeUnit timeUnit) {
        threadPoolExecutor.shutdown();
        while (!threadPoolExecutor.isTerminated()) {
            try {
                threadPoolExecutor.awaitTermination(timeOut, timeUnit);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
