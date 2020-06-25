package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.world.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Builder {

    private volatile World.Dimensions dimensions;
    private final Set<World.Coordinates> walls = new HashSet<>();
    private volatile World.Player player;

    private void handleWorldResized(final Event<WorldResized> event) {
        this.dimensions = event.payload().dimensions();
    }

    private void handleWallBuilt(final Event<WallBuilt> event) {
        this.walls.add(event.payload().location());
    }

    private void handleWallDestroyed(final Event<WallDestroyed> event) {
        this.walls.remove(event.payload().location());
    }

    private void handlePlayerMoved(final Event<PlayerMoved> event) {
        this.player = event.payload().player();
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            switch (event.name().value()) {
                case "WorldResized" -> handleWorldResized((Event<WorldResized>) event);
                case "WallBuilt" -> handleWallBuilt((Event<WallBuilt>)event);
                case "WallDestroyed" -> handleWallDestroyed((Event<WallDestroyed>)event);
                case "PlayerMoved" -> handlePlayerMoved((Event<PlayerMoved>)event);
            };
        }
    }

    public boolean canBuildHere(final World.Coordinates location) {
        if(dimensions == null) {
            //The world hasn't been sized yet!
            return false;
        }
        return dimensions.contains(location) && !walls.contains(location);
    }

    public boolean canDestroyHere(final World.Coordinates location) {
        if(dimensions == null) {
            //The world hasn't been sized yet!
            return false;
        }
        return dimensions.contains(location) && walls.contains(location);
    }

    public boolean canBuildWherePlayerLooking() {
        if(player == null) {
            //Player hasn't spawned yet!
            return false;
        }
        return canBuildHere(player.lookingAt());
    }

    public boolean canDestroyWherePlayerLooking() {
        if(player == null) {
            //Player hasn't spawned yet!
            return false;
        }
        return canDestroyHere(player.lookingAt());
    }

    public World.Coordinates playerTarget() {
        if(player == null) {
            throw new IllegalStateException("Cannot request the player target before they spawn!");
        }
        return player.lookingAt();
    }

}
