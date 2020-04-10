package app.component.output;

import app.component.cruncher.typealias.CruncherResult;

public interface OutputComponent {

    void addToQueue(CruncherResult cruncherResult);

}
