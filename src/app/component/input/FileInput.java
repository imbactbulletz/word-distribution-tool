package app.component.input;

import app.global.Config;

import java.util.ArrayList;
import java.util.List;

public class FileInput implements InputComponent, Runnable {

    private String diskPath;
    private List<String> directoryNames;

    public FileInput(String diskPath) {
        this.diskPath = diskPath;
        this.directoryNames = new ArrayList<>();
    }

    /**
     * {@link FileInput} blocks its thread of execution using this object. Blocking mechanism is performed by releasing
     * the lock on this object in a synchronized block. The thread can reacquire the lock after a certain time (in case wait(millis) is called)
     * or by having another thread call notify() on this object.
     */
    private final Object monitorObject = new Object();

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

    @Override
    public void pause() {
        isPaused = true;
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
                waitToBeStarted();
            }

            scanDirectories();
            if(!isPaused && isRunning) waitForNextScanCycle();
        }

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
                monitorObject.wait(Config.FILE_INPUT_SLEEP_TIME_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("[FileInput] Interrupted while periodically sleeping.");
            }
        }
    }

    private void scanDirectories() {
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Scanned!");
    }

    public String getDiskPath() {
        return diskPath;
    }

    public void setDiskPath(String diskPath) {
        this.diskPath = diskPath;
    }
}