package app.component.cruncher.typealias;

import java.util.HashMap;

public class CrunchResult extends HashMap<BagOfWords, Long> {

    public void combineWith(CrunchResult otherCrunchResult) {
        otherCrunchResult.forEach((key,value) -> this.merge(key, value, Long::sum));
    }
}
