package app.component.cruncher;

import app.component.cruncher.typealias.CrunchResult;
import app.component.input.FileInfo;
import app.component.input.InputComponent;
import app.global.Executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class CounterCruncher implements CruncherComponent, Runnable {

    private int arity;

    private LinkedBlockingQueue<FileInfo> crunchQueue = new LinkedBlockingQueue<>();

    private List<InputComponent> linkedInputComponents = new ArrayList<>();

    public CounterCruncher(int arity) {
        this.arity = arity;
    }

    @Override
    public void run() {
        while (true) {
            try {
                FileInfo fileInfo;
                fileInfo = crunchQueue.take();
                System.out.println("Crunching " + fileInfo.getFileName());
                Future<CrunchResult> crunchResultFuture = Executors.CRUNCHER.submit(new CounterCruncherWorker(arity, fileInfo.getContent()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addToQueue(FileInfo fileInfo) {
        crunchQueue.add(fileInfo);
    }

    @Override
    public int getArity() {
        return arity;
    }

    public List<InputComponent> getLinkedInputComponents() {
        return linkedInputComponents;
    }
}
