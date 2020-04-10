package app.component.cruncher;

import app.component.input.FileInfo;
import app.component.input.InputComponent;
import app.component.output.OutputComponent;

import java.util.List;

public interface CruncherComponent {

    void queueWork(FileInfo fileInfo);

    int getArity();

    void addOutputComponent(OutputComponent outputComponent);
}
