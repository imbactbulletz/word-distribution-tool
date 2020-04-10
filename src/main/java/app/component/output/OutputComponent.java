package app.component.output;

import app.component.cruncher.typealias.CalculationResult;
import app.component.cruncher.typealias.CruncherResult;

import java.util.concurrent.ExecutionException;

public interface OutputComponent {

    void addCruncherResult(CruncherResult cruncherResult);

    CalculationResult poll(String resultName) throws ExecutionException, InterruptedException;

    CalculationResult take(String resultName) throws ExecutionException, InterruptedException;

    void enqueueSumRequest(OutputComponentSumRequest outputComponentSumRequest);
}
