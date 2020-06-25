package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.world.*;

import java.util.*;

public final class Mover {

    private World.Dimensions dimensions;
    private World.Player player;
    private final Set<World.Coordinates> walls = new HashSet<>();

    private void handleWorldResized(final Event<WorldResized> event) {
        this.dimensions = event.payload().dimensions();
    }

    private void handlePlayerMoved(final Event<PlayerMoved> event) {
        this.player = event.payload().player();
    }

    private void handleWallBuilt(final Event<WallBuilt> event) {
        this.walls.add(event.payload().location());
    }

    private void handleWallDestroyed(final Event<WallDestroyed> event) {
        this.walls.remove(event.payload().location());
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            switch (event.name().value()) {
                case "WorldResized" -> handleWorldResized((Event<WorldResized>) event);
                case "PlayerMoved" -> handlePlayerMoved((Event<PlayerMoved>)event);
                case "WallBuilt" -> handleWallBuilt((Event<WallBuilt>)event);
                case "WallDestroyed" -> handleWallDestroyed((Event<WallDestroyed>)event);
            };
        }
    }

    public boolean isFacing(final World.Direction direction) {
        if(player == null) {
            //The player hasn't spawned yet!
            return false;
        }
        return player.orientation() == direction;
    }

    public boolean isLegalMove(final World.Direction direction) {
        if(dimensions == null) {
            //The world hasn't been sized yet!
            return false;
        }
        if(player == null) {
            //The player hasn't spawned yet!
            return false;
        }
        return direction.isLegal(dimensions, player.position(), walls);
    }

}
