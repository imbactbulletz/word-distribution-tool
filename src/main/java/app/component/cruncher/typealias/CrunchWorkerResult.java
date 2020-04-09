package app.component.cruncher.typealias;

import java.util.HashMap;

public class CrunchWorkerResult extends HashMap<BagOfWords, Long> {

    public void combineWith(CrunchWorkerResult otherCrunchWorkerResult) {
        otherCrunchWorkerResult.forEach((key, value) -> this.merge(key, value, Long::sum));
    }
}
