package me.squeeglii.plugin.dislink.util;

import me.squeeglii.plugin.dislink.Dislink;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Little helper for quickly running a pooled thread.
 */
public class Run {

    private final ThreadPoolExecutor threadPoolExecutor;
    private final BlockingQueue<Runnable> taskQueue;

    public Run() {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.threadPoolExecutor = new ThreadPoolExecutor(4, 10, 10, TimeUnit.SECONDS, this.taskQueue);
    }


    public void runAsync(Runnable runnable) {
        this.threadPoolExecutor.execute(runnable);
    }

    public static void async(Runnable runnable) {
        Dislink.plugin().getThreadWatcher().runAsync(runnable);
    }

    public static void sync(Runnable runnable) {
        Dislink.plugin().getServer().getScheduler().runTask(Dislink.plugin(), runnable);
    }

}
