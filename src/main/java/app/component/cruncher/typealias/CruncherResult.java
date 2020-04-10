package app.component.cruncher.typealias;

import java.util.concurrent.Future;

public class CruncherResult {
    private final String jobName;

    private final Future<CalculationResult> calculationResult;

    public CruncherResult(String jobName, Future<CalculationResult> calculationResult) {
        this.jobName = jobName;
        this.calculationResult = calculationResult;
    }

    public String getJobName() {
        return jobName;
    }

    public Future<CalculationResult> getCalculationResult() {
        return calculationResult;
    }
}
