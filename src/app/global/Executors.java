package app.global;

import java.util.concurrent.ExecutorService;

public class Executors {

    static {
        INPUT = java.util.concurrent.Executors.newCachedThreadPool();
    }

    public static final ExecutorService INPUT;
}
