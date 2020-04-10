package app.component.cruncher.typealias;

import java.util.HashMap;

public class CalculationResult extends HashMap<BagOfWords, Long> {

    public void combineWith(HashMap<BagOfWords, Long> otherCrunchWorkerResult) {
        otherCrunchWorkerResult.forEach((key, value) -> this.merge(key, value, Long::sum));
    }
}
