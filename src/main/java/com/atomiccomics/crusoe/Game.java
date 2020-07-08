package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Game {

    public static final class GameState {
        private boolean isRunning = false;
        private boolean playerSelected = false;
        private boolean wallBlueprintActivated = false;

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

        public GameState handleWallBlueprintActivated(final Event<WallBlueprintActivated> event) {
            wallBlueprintActivated = true;
            return this;
        }

        public GameState handleWallBlueprintDeactivated(final Event<WallBlueprintDeactivated> event) {
            wallBlueprintActivated = false;
            return this;
        }

        public GameState process(final List<Event<?>> batch) {
            for(final var event : batch) {
                switch(event.name().value()) {
                    case "GamePaused" -> handleGamePaused((Event<GamePaused>)event);
                    case "GameResumed" -> handleGameResumed((Event<GameResumed>)event);
                    case "PlayerSelected" -> handlePlayerSelected((Event<PlayerSelected>)event);
                    case "PlayerDeselected" -> handlePlayerDeselected((Event<PlayerDeselected>)event);
                    case "WallBlueprintActivated" -> handleWallBlueprintActivated((Event<WallBlueprintActivated>)event);
                    case "WallBlueprintDeactivated" -> handleWallBlueprintDeactivated((Event<WallBlueprintDeactivated>)event);
                }
            }
            return this;
        }
    }

    private final boolean isRunning;
    private final boolean playerSelected;
    private final boolean wallBlueprintActivated;

    public Game(final GameState state) {
        this.isRunning = state.isRunning;
        this.playerSelected = state.playerSelected;
        this.wallBlueprintActivated = state.wallBlueprintActivated;
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
        final var events = new LinkedList<Event<?>>();
        if(playerSelected) {
            return events;
        }
        if(wallBlueprintActivated) {
            events.add(Event.create(new WallBlueprintDeactivated()));
        }
        events.add(Event.create(new PlayerSelected()));
        return events;
    }

    public List<Event<?>> deselectPlayer() {
        if(!playerSelected) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Event.create(new PlayerDeselected()));
    }

    public List<Event<?>> activateWallBlueprint() {
        final var events = new LinkedList<Event<?>>();
        if(wallBlueprintActivated) {
            return events;
        }
        if(playerSelected) {
            events.add(Event.create(new PlayerDeselected()));
        }
        events.add(Event.create(new WallBlueprintActivated()));
        return events;
    }

    public List<Event<?>> deactivateWallBlueprint() {
        if(!wallBlueprintActivated) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Event.create(new WallBlueprintDeactivated()));
    }

}
