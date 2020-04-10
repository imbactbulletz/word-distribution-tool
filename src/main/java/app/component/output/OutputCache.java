package app.component.output;

import app.component.cruncher.typealias.CalculationResult;
import app.component.cruncher.typealias.CruncherResult;
import app.component.cruncher.typealias.CruncherResultPoison;
import app.component.output.result.CategorizedResult;
import app.component.output.result.OutputResultType;
import app.component.output.result.typealias.Cache;
import app.component.output.worker.NotifyUIWorker;
import app.component.output.worker.NotifyUIWorkerImpl;
import app.global.Executors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class OutputCache implements OutputComponent, Runnable {

    private Cache cache = new Cache();

    private LinkedBlockingQueue<OutputComponentTask> outputComponentTasks = new LinkedBlockingQueue<>();

    private NotifyUIWorker notifyUiWorker;

    private volatile boolean hasNotifyUiWorkerFinished = false;

    @Override
    public void run() {
        startUIWorker();

        while (true) {
            try {
                OutputComponentTask outputComponentTask = outputComponentTasks.take();
                if (outputComponentTask instanceof OutputComponentTaskPoison) {
                    notifyUiWorker.shutDown();
                    break;
                }
                System.out.println("Got a task!");

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
    public void addToQueue(CruncherResult cruncherResult) {
        if (cruncherResult instanceof CruncherResultPoison) {
            outputComponentTasks.add(new OutputComponentTaskPoison());
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

    public Cache getCache() {
        return cache;
    }

    public void setHasNotifyUiWorkerFinished() {
        hasNotifyUiWorkerFinished = true;
    }
}
