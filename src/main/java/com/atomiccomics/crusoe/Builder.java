package com.atomiccomics.crusoe;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Builder {

    private World.Dimensions dimensions;
    private final Set<World.Coordinates> walls = new HashSet<>();

    public void handleWorldResized(final Event<WorldResized> event) {
        this.dimensions = event.payload().dimensions();
    }

    public void handleWallBuilt(final Event<WallBuilt> event) {
        this.walls.add(event.payload().location());
    }

    public void handleWallDestroyed(final Event<WallDestroyed> event) {
        this.walls.remove(event.payload().location());
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            switch (event.name().value()) {
                case "WorldResized" -> handleWorldResized((Event<WorldResized>) event);
                case "WallBuilt" -> handleWallBuilt((Event<WallBuilt>)event);
                case "WallDestroyed" -> handleWallDestroyed((Event<WallDestroyed>)event);
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

}
