package com.atomiccomics.crusoe.time;

import com.atomiccomics.crusoe.GamePaused;
import com.atomiccomics.crusoe.GameResumed;
import com.atomiccomics.crusoe.Handler;
import com.atomiccomics.crusoe.RegisteredComponent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
@RegisteredComponent
public final class ExecutorScheduler implements Scheduler {

    private static final System.Logger LOG = System.getLogger(ExecutorScheduler.class.getName());

    private final Set<RepeatingTask> tasks = new CopyOnWriteArraySet<>();
    private final ScheduledExecutorService pool;

    private volatile ScheduledFuture<?> scheduledFuture;

    @Inject
    public ExecutorScheduler(final ScheduledExecutorService pool) {
        this.pool = pool;
    }

    @Override
    public Schedule scheduleRepeatingTask(final RepeatingTask task) {
        tasks.add(task);
        return () -> tasks.remove(task);
    }

    @Handler(GameResumed.class)
    public void resume(final GameResumed event) {
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

    @Handler(GamePaused.class)
    public void pause(final GamePaused event) {
        if(scheduledFuture == null) {
            return;
        }
        scheduledFuture.cancel(false);
        scheduledFuture = null;
    }
}
