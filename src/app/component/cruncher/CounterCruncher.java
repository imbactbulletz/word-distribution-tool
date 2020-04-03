package app.component.cruncher;

import app.component.input.FileInfo;

public class CounterCruncher implements CruncherComponent {

    private int arity;

    public CounterCruncher(int arity) {
        this.arity = arity;
    }

    @Override
    public void addToQueue(FileInfo fileInfo) {

    }

    @Override
    public int getArity() {
        return arity;
    }
}
