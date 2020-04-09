package app.component.cruncher.typealias;

import java.util.concurrent.Future;

public class CruncherResult {
    private final String jobName;
    private final Future<CrunchWorkerResult> crunchWorkerResult;

    public CruncherResult(String jobName, Future<CrunchWorkerResult> crunchWorkerResult) {
        this.jobName = jobName;
        this.crunchWorkerResult = crunchWorkerResult;
    }

    public String getJobName() {
        return jobName;
    }

    public Future<CrunchWorkerResult> getCrunchWorkerResult() {
        return crunchWorkerResult;
    }
}
