package app.component.cruncher;

import app.component.cruncher.typealias.FileInfoPoison;
import app.component.input.FileInfo;
import app.component.input.InputComponent;
import app.global.Executors;
import javafx.application.Platform;
import ui.controller.MainController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class CounterCruncher implements CruncherComponent, Runnable {

    private int arity;

    private LinkedBlockingQueue<FileInfo> crunchQueue = new LinkedBlockingQueue<>();

    private List<InputComponent> linkedInputComponents = new ArrayList<>();

    public CounterCruncher(int arity) {
        this.arity = arity;
    }

    @Override
    public void run() {
        while (true) {
            try {
                FileInfo fileInfo;
                fileInfo = crunchQueue.take();
                if (fileInfo instanceof FileInfoPoison) break;
                System.out.println("Crunching " + fileInfo.getFileName());
                notifyUIOfStartedJob(fileInfo.getFileName(), CruncherJobStatus.IS_CRUNCHING);
                Executors.CRUNCHER.submit(new CounterCruncherWorker(this, arity, fileInfo.getFileName(), 0, fileInfo.getContent(), false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Cruncher died.");
    }

    @Override
    public void addToQueue(FileInfo fileInfo) {
        crunchQueue.add(fileInfo);
    }

    @Override
    public int getArity() {
        return arity;
    }

    public List<InputComponent> getLinkedInputComponents() {
        return linkedInputComponents;
    }

    private void notifyUIOfStartedJob(String jobName, CruncherJobStatus cruncherJobStatus) {
        Platform.runLater(() -> MainController.CRUNCHER_CONTROLLER.refreshJobStatus(this, jobName, cruncherJobStatus));
    }
}
