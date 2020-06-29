package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;

import java.util.Collections;
import java.util.List;

public final class Game {

    public static final class GameState {
        private boolean isRunning = false;
        private boolean playerSelected = false;

        public GameState handleGamePaused(final Event<GamePaused> event) {
            isRunning = false;
            return this;
        }

        public GameState handleGameResumed(final Event<GameResumed> event) {
            isRunning = true;
            return this;
        }

        public GameState handlePlayerSelected(final Event<PlayerSelected> event) {
            playerSelected = true;
            return this;
        }

        public GameState handlePlayerDeselected(final Event<PlayerDeselected> event) {
            playerSelected = false;
            return this;
        }

        public GameState process(final List<Event<?>> batch) {
            for(final var event : batch) {
                switch(event.name().value()) {
                    case "GamePaused" -> handleGamePaused((Event<GamePaused>)event);
                    case "GameResumed" -> handleGameResumed((Event<GameResumed>)event);
                    case "PlayerSelected" -> handlePlayerSelected((Event<PlayerSelected>)event);
                    case "PlayerDeselected" -> handlePlayerDeselected((Event<PlayerDeselected>)event);
                }
            }
            return this;
        }
    }

    private final boolean isRunning;
    private final boolean playerSelected;

    public Game(final GameState state) {
        this.isRunning = state.isRunning;
        this.playerSelected = state.playerSelected;
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

    public List<Event<?>> selectPlayer() {
        if(playerSelected) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Event.create(new PlayerSelected()));
    }

    public List<Event<?>> deselectPlayer() {
        if(!playerSelected) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Event.create(new PlayerDeselected()));
    }

}
