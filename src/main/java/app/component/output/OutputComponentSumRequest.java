package app.component.output;

import java.util.List;

public class OutputComponentSumRequest {

    private final String requestName;

    private final List<String> resultNames;

    public OutputComponentSumRequest(String requestName, List<String> resultNames) {
        this.requestName = requestName;
        this.resultNames = resultNames;
    }

    public String getRequestName() {
        return requestName;
    }

    public List<String> getResultNames() {
        return resultNames;
    }
}
