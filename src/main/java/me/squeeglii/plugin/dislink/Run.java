package me.squeeglii.plugin.dislink;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Run {

    private final ThreadPoolExecutor threadPoolExecutor;
    private final BlockingQueue<Runnable> taskQueue;

    public Run() {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.threadPoolExecutor = new ThreadPoolExecutor(4, 10, 10, TimeUnit.SECONDS, taskQueue);
    }


    public boolean runAsync(Runnable runnable) {
        try {
            return this.taskQueue.offer(runnable, 250, TimeUnit.MILLISECONDS);

        } catch (InterruptedException err) {
            Dislink.get().getLogger()
                    .throwing("ConcurrencyManager", "async", err);

            return false;
        }
    }

    public static boolean async(Runnable runnable) {
        return Dislink.get().getThreadWatcher().runAsync(runnable);
    }

}
