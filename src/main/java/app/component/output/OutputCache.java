package app.component.output;

import app.component.cruncher.typealias.CruncherResult;

import java.util.concurrent.LinkedBlockingQueue;

public class OutputCache implements OutputComponent, Runnable {

    private LinkedBlockingQueue<CruncherResult> cruncherResults = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        while (true) {
            try {
                CruncherResult cruncherResult = this.cruncherResults.take();
                System.out.println("Got a future for " + cruncherResult.getJobName() + " and its working status is " + cruncherResult.getCrunchWorkerResult().isDone());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addToQueue(CruncherResult cruncherResult) {
        cruncherResults.add(cruncherResult);
    }
}
