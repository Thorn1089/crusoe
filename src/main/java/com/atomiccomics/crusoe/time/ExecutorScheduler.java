package com.atomiccomics.crusoe.time;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ExecutorScheduler implements Scheduler {

    private static final System.Logger LOG = System.getLogger(ExecutorScheduler.class.getName());

    private final Set<RepeatingTask> tasks = new CopyOnWriteArraySet<>();
    private final ScheduledExecutorService pool;

    private volatile ScheduledFuture<?> scheduledFuture;

    public ExecutorScheduler(final ScheduledExecutorService pool) {
        this.pool = pool;
    }

    @Override
    public Schedule scheduleRepeatingTask(final RepeatingTask task) {
        tasks.add(task);
        return () -> tasks.remove(task);
    }

    public void start() {
        if (scheduledFuture != null) {
            return;
        }
        scheduledFuture = pool.scheduleAtFixedRate(() -> {
            try {
                for(final var task : tasks) {
                    final var isDone = task.doWork();
                    if(isDone) {
                        tasks.remove(task);
                    }
                }
            } catch (final Exception e) {
                LOG.log(System.Logger.Level.ERROR, "Encountered an error during scheduled task", e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        if(scheduledFuture == null) {
            return;
        }
        scheduledFuture.cancel(true);
        scheduledFuture = null;
    }

    public boolean isRunning() {
        return scheduledFuture != null;
    }
}
