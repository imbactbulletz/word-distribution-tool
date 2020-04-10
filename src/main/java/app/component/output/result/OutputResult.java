package app.component.output.result;

import java.util.Objects;

public class OutputResult {
    private String resultName;

    private double progress;

    private OutputResultType resultType;

    public OutputResult(String resultName, double progress, OutputResultType resultType) {
        this.resultName = resultName;
        this.progress = progress;
        this.resultType = resultType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutputResult that = (OutputResult) o;
        return resultName.equals(that.resultName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultName);
    }

    public String getResultName() {
        return resultName;
    }

    public double getProgress() {
        return progress;
    }

    public OutputResultType getResultType() {
        return resultType;
    }
}
