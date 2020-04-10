package app.global;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class Executors {

    static {
        COMPONENT = java.util.concurrent.Executors.newCachedThreadPool();
        INPUT = java.util.concurrent.Executors.newCachedThreadPool();
        CRUNCHER = new ForkJoinPool();
        OUTPUT = java.util.concurrent.Executors.newCachedThreadPool();
    }

    public static final ExecutorService COMPONENT;

    public static final ExecutorService INPUT;

    public static final ForkJoinPool CRUNCHER;

    public static final ExecutorService OUTPUT;
}
