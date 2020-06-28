package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;

import java.util.Collections;
import java.util.List;

public final class Game {

    public static final class GameState {
        private boolean isRunning = false;

        public GameState handleGamePaused(final Event<GamePaused> event) {
            isRunning = false;
            return this;
        }

        public GameState handleGameResumed(final Event<GameResumed> event) {
            isRunning = true;
            return this;
        }

        public GameState process(final List<Event<?>> batch) {
            for(final var event : batch) {
                switch(event.name().value()) {
                    case "GamePaused" -> handleGamePaused((Event<GamePaused>)event);
                    case "GameResumed" -> handleGameResumed((Event<GameResumed>)event);
                }
            }
            return this;
        }
    }

    private final boolean isRunning;

    public Game(final GameState state) {
        this.isRunning = state.isRunning;
    }

    public List<Event<?>> pause() {
        if(!isRunning) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Event.create(new GamePaused()));
    }

    public List<Event<?>> resume() {
        if(isRunning) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Event.create(new GameResumed()));
    }

}
