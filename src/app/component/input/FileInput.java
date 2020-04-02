package app.component.input;

import app.component.cruncher.CruncherComponent;
import app.global.Config;
import app.global.Executors;
import javafx.application.Platform;
import ui.controller.MainController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class FileInput implements InputComponent, Runnable {

    private final Object monitorObject = new Object();

    private final Object workerMonitorObject = new Object();

    private String diskPath;

    private List<File> directories;

    private Map<File, Long> cache = new HashMap<>();

    private List<CruncherComponent> cruncherComponents = new CopyOnWriteArrayList<>();

    private List<File> filesForReading = new CopyOnWriteArrayList<>();

    private InputComponentState state;

    private volatile boolean isPaused = true;

    private volatile boolean isRunning = true;

    private volatile boolean hasActiveWorker = false;

    public FileInput(String diskPath) {
        this.diskPath = diskPath;
        this.directories = new CopyOnWriteArrayList<>();
    }

    @Override
    public void pause() {
        isPaused = true;
        // in case component is doing a periodic wait
        wakeUpFileInputComponent();
    }

    @Override
    public void resume() {
        isPaused = false;
        wakeUpFileInputComponent();
    }

    private void wakeUpFileInputComponent() {
        synchronized (monitorObject) {
            monitorObject.notify();
        }
    }

    @Override
    public void shutdown() {
        isRunning = false;
        resume();
    }

    @Override
    public void run() {
        waitToBeStarted();

        while (isRunning) {
            while (isPaused) {
                waitToBeResumed();
            }

            if (isPaused || !isRunning) continue;
            List<File> filesInDirectories = scanDirectories();
            if (isPaused || !isRunning) continue;
            List<File> changedFiles = getChangedFiles(filesInDirectories);
            if (isPaused || !isRunning) continue;
            if (changedFiles.size() > 0) scheduleFilesForReading(changedFiles);
            // scan all files
            // check if they should be scanned (if they are not currently in the cache or have same last date modified)
            if (!isPaused && isRunning) waitForNextScanCycle();
        }

        if (hasActiveWorker) {
            waitForWorkerToFinish();
        }

        notifyUIOfComponentFinished(this);
        System.out.println("File input has been shut down.");
    }

    private void waitToBeStarted() {
        synchronized (monitorObject) {
            try {
                state = InputComponentState.NOT_STARTED;
                notifyUIOfCurrentState();
                monitorObject.wait();
                state = InputComponentState.WORKING;
                notifyUIOfCurrentState();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("[FileInput] Interrupted while waiting to be started.");
            }
        }
    }

    private void waitToBeResumed() {
        synchronized (monitorObject) {
            try {
                state = InputComponentState.PAUSED;
                notifyUIOfCurrentState();
                monitorObject.wait();
                state = InputComponentState.WORKING;
                notifyUIOfCurrentState();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("[FileInput] Interrupted while waiting to be started.");
            }
        }
    }

    private void waitForNextScanCycle() {
        synchronized (monitorObject) {
            try {
                monitorObject.wait(Config.FILE_INPUT_SLEEP_TIME_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("[FileInput] Interrupted while periodically sleeping.");
            }
        }
    }

    private List<File> scanDirectories() {
        List<File> filesForInspection = new ArrayList<>();

        for (File directory : directories) {
            if (isPaused && isRunning) {
                return new ArrayList<>();
            }

            List<File> files = scanDirectory(directory);

            if (files != null) {
                filesForInspection.addAll(files);
            }
        }

        return filesForInspection;
    }

    private List<File> scanDirectory(File directory) {
        try {
            return Files.walk(Paths.get(directory.getPath()))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void scheduleFilesForReading(List<File> files) {
        filesForReading.addAll(files);
        if (!hasActiveWorker) {
            hasActiveWorker = true;
            Executors.INPUT.submit(new FileInputReadWorker(this, filesForReading, workerMonitorObject));
        }
    }

    private List<File> getChangedFiles(List<File> files) {
        List<File> changedFiles = new ArrayList<>();

        for (File file : files) {
            if (isPaused || !isRunning) return new ArrayList<>();

            if (cache.get(file) == null || cache.get(file) != file.lastModified()) {
                changedFiles.add(file);
                cache.put(file, file.lastModified());
            }
        }

        return changedFiles;
    }

    private void waitForWorkerToFinish() {
        synchronized (workerMonitorObject) {
            try {
                workerMonitorObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyUIOfCurrentState() {
        Platform.runLater(() -> {
            MainController.INPUT_CONTROLLER.refreshEntryState(this, state);
        });
    }

    private void notifyUIOfComponentFinished(InputComponent inputComponent) {
        Platform.runLater(() -> {
            MainController.INPUT_CONTROLLER.removeComponent(inputComponent);
        });
    }

    public String getDiskPath() {
        return diskPath;
    }

    public void addDirectory(File directory) {
        directories.add(directory);
    }

    public void removeDirectory(File directory) {
        List<File> directoryFiles = scanDirectory(directory);

        for (File file : directoryFiles) {
            cache.remove(file);
        }

        directories.remove(directory);
    }

    public List<File> getDirectories() {
        return directories;
    }

    public List<CruncherComponent> getCruncherComponents() {
        return cruncherComponents;
    }

    public void setHasActiveWorker(boolean hasActiveWorker) {
        this.hasActiveWorker = hasActiveWorker;
    }
}