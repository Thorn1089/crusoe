package com.atomiccomics.crusoe;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class Mover {

    private World.Dimensions dimensions;
    private World.Coordinates location;

    public void handleWorldResized(final Event<WorldResized> event) {
        this.dimensions = event.payload().dimensions();
    }

    public void handlePlayerMoved(final Event<PlayerMoved> event) {
        this.location = event.payload().location();
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            switch (event.name().value()) {
                case "WorldResized" -> handleWorldResized((Event<WorldResized>) event);
                case "PlayerMoved" -> handlePlayerMoved((Event<PlayerMoved>)event);
            };
        }
    }

    public Set<World.Direction> legalMoves() {
        if(dimensions == null) {
            //The world hasn't been sized yet!
            return Collections.emptySet();
        }
        if(location == null) {
            //The player hasn't spawned yet!
            return Collections.emptySet();
        }
        final var moves = EnumSet.allOf(World.Direction.class);
        for(final var move : World.Direction.values()) {
            if(!move.isLegal(dimensions, location)) {
                moves.remove(move);
            }
        }
        return moves;
    }

}
