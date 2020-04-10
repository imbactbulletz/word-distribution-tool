package app.component.output;

import app.component.cruncher.typealias.CalculationResult;
import app.component.cruncher.typealias.CruncherResult;
import app.component.cruncher.typealias.CruncherResultPoison;
import app.component.output.result.CategorizedResult;
import app.component.output.result.OutputResultType;
import app.component.output.result.typealias.Cache;
import app.component.output.worker.NotifyUIWorker;
import app.component.output.worker.NotifyUIWorkerImpl;
import app.component.output.worker.SumWorker;
import app.global.Executors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class OutputCache implements OutputComponent, Runnable {

    private Cache cache = new Cache();

    private LinkedBlockingQueue<OutputComponentSumRequest> outputComponentSumRequests = new LinkedBlockingQueue<>();

    private NotifyUIWorker notifyUiWorker;

    private volatile boolean hasNotifyUiWorkerFinished = false;

    private AtomicInteger atomicInteger = new AtomicInteger();

    @Override
    public void run() {
        startUIWorker();

        while (true) {
            try {
                OutputComponentSumRequest outputComponentSumRequest = outputComponentSumRequests.take();
                if (outputComponentSumRequest instanceof OutputComponentSumRequestPoison) {
                    notifyUiWorker.shutDown();
                    break;
                }

                System.out.println("Got a task!");
                Future<CalculationResult> categorizedResultFuture = Executors.OUTPUT.submit(new SumWorker(outputComponentSumRequest, cache, atomicInteger));
                cache.put(outputComponentSumRequest.getRequestName(), new CategorizedResult(categorizedResultFuture, OutputResultType.COMBINED));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while(!hasNotifyUiWorkerFinished);
        System.out.println("Output component died.");
    }

    private void startUIWorker() {
        NotifyUIWorkerImpl notifyUiWorkerImpl = new NotifyUIWorkerImpl(this);
        notifyUiWorker = notifyUiWorkerImpl;
        Executors.OUTPUT.submit(notifyUiWorkerImpl);
    }

    @Override
    public void addCruncherResult(CruncherResult cruncherResult) {
        if (cruncherResult instanceof CruncherResultPoison) {
            outputComponentSumRequests.add(new OutputComponentSumRequestPoison(null, null));
        } else {
            cache.put(cruncherResult.getJobName(), new CategorizedResult(cruncherResult.getCalculationResult(), OutputResultType.SINGLE));
        }
    }

    @Override
    public CalculationResult poll(String resultName) throws ExecutionException, InterruptedException {
        CategorizedResult categorizedResult = cache.get(resultName);

        if(categorizedResult.getCalculationResultFuture().isDone()) {
            return categorizedResult.getCalculationResultFuture().get();
        }

        return null;
    }

    @Override
    public CalculationResult take(String resultName) throws ExecutionException, InterruptedException {
        CategorizedResult categorizedResult = cache.get(resultName);
        return categorizedResult.getCalculationResultFuture().get();
    }

    @Override
    public void enqueueSumRequest(OutputComponentSumRequest outputComponentSumRequest) {
        outputComponentSumRequests.add(outputComponentSumRequest);
    }

    public Cache getCache() {
        return cache;
    }

    public void setHasNotifyUiWorkerFinished() {
        hasNotifyUiWorkerFinished = true;
    }
}
