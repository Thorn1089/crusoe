package com.atomiccomics.crusoe;

import java.util.*;

public final class Mover {

    private World.Dimensions dimensions;
    private World.Player player;
    private final Set<World.Coordinates> walls = new HashSet<>();

    public void handleWorldResized(final Event<WorldResized> event) {
        this.dimensions = event.payload().dimensions();
    }

    public void handlePlayerMoved(final Event<PlayerMoved> event) {
        this.player = event.payload().player();
    }

    public void handleWallBuilt(final Event<WallBuilt> event) {
        this.walls.add(event.payload().location());
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            switch (event.name().value()) {
                case "WorldResized" -> handleWorldResized((Event<WorldResized>) event);
                case "PlayerMoved" -> handlePlayerMoved((Event<PlayerMoved>)event);
                case "WallBuilt" -> handleWallBuilt((Event<WallBuilt>)event);
            };
        }
    }

    public Set<World.Direction> legalMoves() {
        if(dimensions == null) {
            //The world hasn't been sized yet!
            return Collections.emptySet();
        }
        if(player == null) {
            //The player hasn't spawned yet!
            return Collections.emptySet();
        }
        final var moves = EnumSet.allOf(World.Direction.class);
        for(final var move : World.Direction.values()) {
            if(!move.isLegal(dimensions, player.position(), walls)) {
                moves.remove(move);
            }
        }
        return moves;
    }

}
