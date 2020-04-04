package app.global;

import java.util.concurrent.ExecutorService;

public class Executors {

    static {
        COMPONENT = java.util.concurrent.Executors.newCachedThreadPool();
        INPUT = java.util.concurrent.Executors.newCachedThreadPool();
        CRUNCHER = java.util.concurrent.Executors.newCachedThreadPool();
    }

    public static final ExecutorService COMPONENT;

    public static final ExecutorService INPUT;

    public static final ExecutorService CRUNCHER;
}
