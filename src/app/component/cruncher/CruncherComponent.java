package app.component.cruncher;

import app.component.input.FileInfo;
import app.component.input.InputComponent;

import java.util.List;

public interface CruncherComponent {

    void addToQueue(FileInfo fileInfo);

    int getArity();

    List<InputComponent> getLinkedInputComponents();
}
