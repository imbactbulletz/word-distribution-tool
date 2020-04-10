package app.component.cruncher;

import app.component.cruncher.typealias.CalculationResult;
import app.component.cruncher.typealias.CruncherResult;
import app.component.cruncher.typealias.CruncherResultPoison;
import app.component.input.FileInfoPoison;
import app.component.input.FileInfo;
import app.component.output.OutputComponent;
import app.global.Executors;
import javafx.application.Platform;
import ui.controller.MainController;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class CounterCruncher implements CruncherComponent, Runnable {

    private int arity;

    private LinkedBlockingQueue<FileInfo> crunchQueue = new LinkedBlockingQueue<>();

    private List<OutputComponent> linkedOutputComponents = new CopyOnWriteArrayList<>();

    public CounterCruncher(int arity) {
        this.arity = arity;
    }

    @Override
    public void run() {
        while (true) {
            try {
                FileInfo fileInfo;
                fileInfo = crunchQueue.take();
                if (fileInfo instanceof FileInfoPoison) {
                    poisonLinkedOutputComponents();
                    break;
                }
                System.out.println("Crunching " + fileInfo.getFileName());
                notifyUIOfStartedJob(fileInfo.getFileName());

                Future<CalculationResult> cruncherResultFuture = Executors.CRUNCHER.submit(new CounterCruncherWorker(this, arity, fileInfo.getFileName(), 0, fileInfo.getContent(), false));
                CruncherResult cruncherResult = new CruncherResult(fileInfo.getFileName() + " - arity" + arity, cruncherResultFuture);
                for (OutputComponent outputComponent: linkedOutputComponents) {
                    outputComponent.addCruncherResult(cruncherResult);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Cruncher died.");
    }

    @Override
    public void queueWork(FileInfo fileInfo) {
        crunchQueue.add(fileInfo);
    }

    @Override
    public int getArity() {
        return arity;
    }

    @Override
    public void addOutputComponent(OutputComponent outputComponent) {
        linkedOutputComponents.add(outputComponent);
    }

    private void notifyUIOfStartedJob(String jobName) {
        Platform.runLater(() -> MainController.CRUNCHER_CONTROLLER.refreshJobStatus(this, jobName, CruncherJobStatus.IS_CRUNCHING));
    }

    private void poisonLinkedOutputComponents() {
        for(OutputComponent outputComponent: linkedOutputComponents) {
            outputComponent.addCruncherResult(new CruncherResultPoison());
        }
    }
}
