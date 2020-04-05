package app.component.cruncher.typealias;

import java.io.BufferedReader;
import java.util.ArrayList;

public class BagOfWords extends ArrayList<String> {

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;

        if (!(o instanceof ArrayList<?>)) return false;
        // not the same parameterized type
        if (!(((ArrayList<?>) o).get(0) instanceof String)) return false;
        // not the same size
        if (((ArrayList<String>)o).size() != this.size()) return false;
        // don't contain the same elements
        for (String string: this) {
            if(!((ArrayList<String>) o).contains(string)) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int sum = 0;

        for (String item: this) {
            sum += item.hashCode();
        }

        return sum;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("[");
        for (int itemIndex = 0; itemIndex < this.size() - 1; itemIndex++) {
            stringBuffer.append(this.get(itemIndex));
            stringBuffer.append(", ");
        }
        stringBuffer.append(this.get(this.size() - 1));
        stringBuffer.append("]");

        return stringBuffer.toString();
    }
}

