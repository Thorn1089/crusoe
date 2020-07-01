package com.atomiccomics.crusoe;

import com.google.inject.Inject;

import java.util.concurrent.ScheduledExecutorService;

public final class ThreadReaper {

    private final ScheduledExecutorService schedulerPool;

    @Inject
    public ThreadReaper(final ScheduledExecutorService schedulerPool) {
        this.schedulerPool = schedulerPool;
    }

    @Cleanup
    public void shutdown() {
        schedulerPool.shutdownNow();
    }
}
