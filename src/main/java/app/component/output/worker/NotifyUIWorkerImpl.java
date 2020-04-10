package app.component.output.worker;

import app.component.output.OutputCache;
import app.component.output.result.CategorizedResult;
import app.component.output.result.OutputResult;
import app.component.output.result.OutputResultType;
import com.google.common.collect.ImmutableMap;
import javafx.application.Platform;
import ui.controller.OutputController;

public class NotifyUIWorkerImpl implements Runnable, NotifyUIWorker {

    private final OutputCache outputCache;

    private boolean isRunning = true;

    public NotifyUIWorkerImpl(OutputCache outputCache) {
        this.outputCache = outputCache;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                notifyUIOfChanges();
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        outputCache.setHasNotifyUiWorkerFinished();
        System.out.println("NotifyUIWorker has finished.");
    }

    private void notifyUIOfChanges() {
        ImmutableMap<String, CategorizedResult> cacheCopy = ImmutableMap.copyOf(outputCache.getCache());
        cacheCopy.forEach((resultName, categorizedResult) -> {
            double progress;
            if (categorizedResult.getResultType() == OutputResultType.SINGLE) {
                if (categorizedResult.getCalculationResultFuture() != null) {
                    progress = categorizedResult.getCalculationResultFuture().isDone() ? 1 : 0;
                } else {
                    progress = 0;
                }
            } else {
                progress = categorizedResult.getProgress();
            }

            notifyUIOfResultUpdated(new OutputResult(resultName, progress, categorizedResult.getResultType()));
        });
    }

    private void notifyUIOfResultUpdated(OutputResult outputResult) {
        Platform.runLater(() -> OutputController.updateOutputResult(outputResult));
    }

    @Override
    public void shutDown() {
        isRunning = false;
    }
}
