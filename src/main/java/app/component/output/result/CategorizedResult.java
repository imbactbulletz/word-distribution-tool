package app.component.output.result;

import app.component.cruncher.typealias.CalculationResult;

import java.util.concurrent.Future;

 public class CategorizedResult {

    private final Future<CalculationResult> calculationResultFuture;

    private final OutputResultType resultType;

    private volatile double progress;

    public CategorizedResult(Future<CalculationResult> calculationResultFuture, OutputResultType resultType) {
        this.calculationResultFuture = calculationResultFuture;
        this.resultType = resultType;
    }

    public Future<CalculationResult> getCalculationResultFuture() {
        return calculationResultFuture;
    }

    public OutputResultType getResultType() {
        return resultType;
    }

    public double getProgress() {
        return progress;
    }

     public void setProgress(double progress) {
         this.progress = progress;
     }
 }
