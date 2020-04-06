package app.component.input;

import app.component.cruncher.CruncherComponent;
import app.global.Executors;
import javafx.application.Platform;
import ui.controller.MainController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
            FileInfo fileInfo = new FileInfo(file.getName(), file.getAbsolutePath(), readFile(file.getPath()));
            for (CruncherComponent cruncherComponent : fileInput.getCruncherComponents()) {
                cruncherComponent.addToQueue(fileInfo);
            }
        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
            System.exit(-404);
        }

        if (files.size() > 0) {
            Executors.INPUT.submit(new FileInputReadWorker(fileInput, files, monitorObject));
        } else {
            fileInput.setHasActiveWorker(false);
            notifyUI("Idle");
            // finished, tell file input component that it can finish if it is waiting
            synchronized (monitorObject) {
                monitorObject.notify();
            }
        }
    }

    private String readFile(String pathname) throws IOException, OutOfMemoryError {
        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int) file.length());

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                fileContents.append(currentLine).append(System.lineSeparator());
            }
            return fileContents.toString();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            System.exit(-404);
            return null;
        }
    }

    private void notifyUI(String message) {
        Platform.runLater(() -> MainController.INPUT_CONTROLLER.refreshEntryStatus(fileInput, message));
    }
}
