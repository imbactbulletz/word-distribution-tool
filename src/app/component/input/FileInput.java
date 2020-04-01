package app.component.input;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class FileInput implements InputComponent, Runnable {

    private final Object monitorObject = new Object();
    /**
     * {@link FileInput} blocks its thread of execution using this object. Blocking mechanism is performed by releasing
     * the lock on this object in a synchronized block. The thread can reacquire the lock after a certain time (in case wait(millis) is called)
     * or by having another thread call notify() on this object.
     */
    private String diskPath;
    private List<File> directories;
    private Map<File, Long> cache = new HashMap<>();
    /**
     * Another thread sets this flag to tell {@link FileInput} to discontinue any ongoing work and
     * stop its execution for an unspecified amount of time.
     */
    private volatile boolean isPaused = true;
    /**
     * Another thread sets this flag to indicate whether {@link FileInput} should discontinue ongoing work and end its
     * execution permanently.
     */
    private volatile boolean isRunning = true;


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

    /**
     * Tells the execution thread of File Input component to reacquire
     * its lock on the monitor object.
     */
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
                notifyUI("Paused");
                waitToBeStarted();
            }

            if (isPaused || !isRunning) continue;
            List<File> filesInDirectories = scanDirectories();
            if (isPaused || !isRunning) continue;
            List<File> changedFiles = getChangedFiles(filesInDirectories);
            if (isPaused || !isRunning) continue;
            List<FileInfo> readFiles = readFiles(changedFiles);

            // scan all files
            // check if they should be scanned (if they are not currently in the cache or have same last date modified)
            if (!isPaused && isRunning) waitForNextScanCycle();
        }

        notifyUIOfComponentFinished(this);
        System.out.println("File input has been shut down.");
    }

    /**
     * Blocks thread from further execution by releasing the lock on the monitor object.
     */
    private void waitToBeStarted() {
        synchronized (monitorObject) {
            try {
                monitorObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("[FileInput] Interrupted while waiting to be started.");
            }
        }
    }

    /**
     * Releases the lock on monitor object for an amount of time.
     */
    private void waitForNextScanCycle() {
        synchronized (monitorObject) {
            try {
                notifyUI("Idle");
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

            notifyUI("Scanning " + directory.getName());
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
            return null;
        }
    }

    private List<FileInfo> readFiles(List<File> files) {
        List<FileInfo> fileInfos = new ArrayList<>();
        List<File> readFiles = new ArrayList<>();

        for (File file : files) {
            notifyUI("Reading " + file.getName());
            Future<FileInfo> resultFuture = Executors.INPUT.submit(new FileInputReadWorker(file));
            try {
                if (isPaused || !isRunning) {
                    // remove all unread files from cache so they can be read next time
                    List<File> unreadFiles = new ArrayList<>(files);
                    unreadFiles.removeAll(readFiles);
                    for(File unreadFile : unreadFiles) {
                        cache.remove(unreadFile);
                    }
                    return null;
                }

                FileInfo fileInfo = resultFuture.get();
                fileInfos.add(fileInfo);
                readFiles.add(file);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return fileInfos;
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


    private void notifyUI(String statusMessage) {
        Platform.runLater(() -> {
            MainController.INPUT_CONTROLLER.refreshEntry(this, statusMessage);
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
        directories.remove(directory);
    }

    public List<File> getDirectories() {
        return directories;
    }
}