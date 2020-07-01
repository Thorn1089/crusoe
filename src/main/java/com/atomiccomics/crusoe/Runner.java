package com.atomiccomics.crusoe;

import com.google.inject.Singleton;

@Singleton
@RegisteredComponent
public final class Runner {

    private volatile boolean isRunning = false;

    @Handler(GamePaused.class)
    public void handleGamePaused(final GamePaused event) {
        isRunning = false;
    }

    @Handler(GameResumed.class)
    public void handleGameResumed(final GameResumed event) {
        isRunning = true;
    }

    public boolean isGameRunning() {
        return isRunning;
    }

}
