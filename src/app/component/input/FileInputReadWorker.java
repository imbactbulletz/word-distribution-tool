package app.component.input;

import app.component.cruncher.CruncherComponent;
import app.global.Executors;
import javafx.application.Platform;
import ui.controller.MainController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileInputReadWorker implements Runnable {

    private final FileInput fileInput;
    private final List<File> files;
    private final Object monitorObject;

    public FileInputReadWorker(FileInput fileInput, List<File> files, Object monitorObject) {
        this.fileInput = fileInput;
        this.files = files;
        this.monitorObject = monitorObject;
    }

    @Override
    public void run() {
        File file = files.get(0);
        files.remove(0);

        try {
            notifyUI("Reading " + file.getName());
            String content = Files.readString(Paths.get(file.getPath()));
            FileInfo fileInfo = new FileInfo(file.getName(), file.getAbsolutePath(), content);

            for (CruncherComponent cruncherComponent: fileInput.getCruncherComponents()) {
                cruncherComponent.addToQueue(fileInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(files.size() > 0) {
            Executors.INPUT.submit(new FileInputReadWorker(fileInput, files, monitorObject));
        } else {
            fileInput.setHasActiveWorker(false);
            // finished, tell file input component that it can finish if it is waiting
            synchronized (monitorObject) {
                monitorObject.notify();
            }
        }
    }

    private void notifyUI(String message) {
        Platform.runLater(() -> {
            MainController.INPUT_CONTROLLER.refreshEntryStatus(fileInput, message);
        });
    }
}
