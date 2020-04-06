package app.component.input;

import app.component.cruncher.CruncherComponent;
import app.global.Executors;
import javafx.application.Platform;
import ui.controller.MainController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

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

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine()).append(System.lineSeparator());
            }
            return fileContents.toString();
        }
    }

    private void notifyUI(String message) {
        Platform.runLater(() -> {
            MainController.INPUT_CONTROLLER.refreshEntryStatus(fileInput, message);
        });
    }
}
