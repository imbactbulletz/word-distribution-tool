package app.component.output.worker;

import app.component.cruncher.typealias.CalculationResult;
import app.component.output.OutputComponentSumRequest;
import app.component.output.result.CategorizedResult;
import app.component.output.result.typealias.Cache;
import javafx.application.Platform;
import ui.controller.MainController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class SumWorker implements Callable<CalculationResult> {

    private final Cache cache;

    private final AtomicInteger remainingWorkers;

    private final OutputComponentSumRequest outputComponentSumRequest;

    public SumWorker(OutputComponentSumRequest outputComponentSumRequest, Cache cache, AtomicInteger remainingWorkers) {
        this.cache = cache;
        this.remainingWorkers = remainingWorkers;
        this.outputComponentSumRequest = outputComponentSumRequest;
    }

    @Override
    public CalculationResult call() throws Exception {
        CalculationResult result = new CalculationResult();
        CategorizedResult categorizedResult = cache.get(outputComponentSumRequest.getRequestName());

        List<Future<CalculationResult>> calculationResultFutures = getCalculationResultFutures();

        for(int i = 0; i < calculationResultFutures.size(); i++) {
            CalculationResult calculationResult = calculationResultFutures.get(i).get();
            double progress = (double)(i+1) / calculationResultFutures.size();
            categorizedResult.setProgress(progress);
            notifyUIToUpdateProgressBar(progress);
            result.combineWith(calculationResult);
        }

        return result;
    }

    private List<Future<CalculationResult>> getCalculationResultFutures() {
        List<Future<CalculationResult>> calculationResultFutures = new ArrayList<>();
        for(String resultName: outputComponentSumRequest.getResultNames()) {
            CategorizedResult categorizedResult = cache.get(resultName);
            calculationResultFutures.add(categorizedResult.getCalculationResultFuture());
        }
        return calculationResultFutures;
    }

    private void notifyUIToUpdateProgressBar(double value) {
        Platform.runLater(() -> MainController.OUTPUT_CONTROLLER.updateProgressBar(value));
    }
}
