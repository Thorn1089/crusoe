package com.atomiccomics.crusoe;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public final class World {

    public record Coordinates(int x, int y) {
        public Coordinates {
            if(x < 0) {
                throw new IllegalArgumentException("X coordinate cannot be negative");
            }
            if(y < 0) {
                throw new IllegalArgumentException("Y coordinate cannot be negative");
            }
        }

        public Coordinates moveNorth() {
            return new Coordinates(x, y + 1);
        }

        public Coordinates moveSouth() {
            return new Coordinates(x, y - 1);
        }

        public Coordinates moveWest() {
            return new Coordinates(x - 1, y);
        }

        public Coordinates moveEast() {
            return new Coordinates(x + 1, y);
        }

        public Coordinates moveNortheast() {
            return moveNorth().moveEast();
        }

        public Coordinates moveNorthwest() {
            return moveNorth().moveWest();
        }

        public Coordinates moveSoutheast() {
            return moveSouth().moveEast();
        }

        public Coordinates moveSouthwest() {
            return moveSouth().moveWest();
        }
    }

    public record Dimensions(int width, int height) {
        public Dimensions {
            if(width <= 0) {
                throw new IllegalArgumentException("Width must be positive and non-zero");
            }
            if(height <= 0) {
                throw new IllegalArgumentException("Height must be positive and non-zero");
            }
        }

        public boolean contains(final Coordinates coordinates) {
            return coordinates.x() < width && coordinates.y() < height;
        }
    }

    public static final class WorldState {
        private volatile Dimensions dimensions;
        private volatile Coordinates location;
        private final Set<Coordinates> walls = new CopyOnWriteArraySet<>();

        public WorldState handleWorldResized(final Event<WorldResized> event) {
            this.dimensions = event.payload().dimensions();
            return this;
        }

        public WorldState handlePlayerMoved(final Event<PlayerMoved> event) {
            this.location = event.payload().location();
            return this;
        }

        public WorldState handleWallBuilt(final Event<WallBuilt> event) {
            this.walls.add(event.payload().location());
            return this;
        }

        public WorldState process(final List<Event<?>> batch) {
            WorldState updatedState = this;
            for(final var event : batch) {
                updatedState = switch (event.name().value()) {
                    case "WorldResized" -> updatedState.handleWorldResized((Event<WorldResized>) event);
                    case "PlayerMoved" -> updatedState.handlePlayerMoved((Event<PlayerMoved>)event);
                    case "WallBuilt" -> updatedState.handleWallBuilt((Event<WallBuilt>)event);
                    default -> updatedState;
                };
            }
            return updatedState;
        }

        public Dimensions dimensions() {
            return dimensions;
        }

        public Coordinates location() {
            return location;
        }

        public Set<Coordinates> walls() {
            return new HashSet<>(walls);
        }
    }

    public enum Direction {
        NORTH {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location, Set<Coordinates> obstacles) {
                return dimensions.contains(location.moveNorth()) && !obstacles.contains(location.moveNorth());
            }
        }, SOUTH {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location, Set<Coordinates> obstacles) {
                return dimensions.contains(location.moveSouth()) && !obstacles.contains(location.moveSouth());
            }
        }, WEST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location, Set<Coordinates> obstacles) {
                return dimensions.contains(location.moveWest()) && !obstacles.contains(location.moveWest());
            }
        }, EAST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location, Set<Coordinates> obstacles) {
                return dimensions.contains(location.moveEast()) && !obstacles.contains(location.moveEast());
            }
        }, NORTHEAST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location, Set<Coordinates> obstacles) {
                return dimensions.contains(location.moveNortheast()) && !obstacles.contains(location.moveNortheast());
            }
        }, SOUTHEAST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location, Set<Coordinates> obstacles) {
                return dimensions.contains(location.moveSoutheast()) && !obstacles.contains(location.moveSoutheast());
            }
        }, NORTHWEST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location, Set<Coordinates> obstacles) {
                return dimensions.contains(location.moveNorthwest()) && !obstacles.contains(location.moveNorthwest());
            }
        }, SOUTHWEST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location, Set<Coordinates> obstacles) {
                return dimensions.contains(location.moveSouthwest()) && !obstacles.contains(location.moveSouthwest());
            }
        };

        public abstract boolean isLegal(Dimensions dimensions, Coordinates location, Set<Coordinates> obstacles);
    }

    private final Dimensions dimensions;
    private final Coordinates location;
    private final Set<Coordinates> walls;

    public World(final WorldState state) {
        this.dimensions = state.dimensions();
        this.location = state.location();
        this.walls = state.walls();
    }

    public List<Event<?>> resize(final Dimensions dimensions) {
        if(Objects.equals(dimensions, this.dimensions)) {
            return Collections.emptyList();
        }

        if(this.dimensions == null) {
            return Collections.singletonList(Event.create(new WorldResized(dimensions)));
        }

        if(dimensions.width() < this.dimensions.width() || dimensions.height() < this.dimensions.height()) {
            final var newX = Math.min(location.x(), dimensions.width() - 1);
            final var newY = Math.min(location.y(), dimensions.height() - 1);
            final var newLocation = new Coordinates(newX, newY);

            if(!Objects.equals(location, newLocation)) {
                return Arrays.asList(
                        Event.create(new PlayerMoved(newLocation)),
                        Event.create(new WorldResized(dimensions)));
            }
        }

        return Collections.singletonList(Event.create(new WorldResized(dimensions)));
    }

    public List<Event<?>> move(final Direction direction) {
        if (!direction.isLegal(dimensions, location, walls)) {
            throw new IllegalStateException("Can't move into this spot!");
        }

        return Collections.singletonList(Event.create(new PlayerMoved(switch (direction) {
            case NORTH -> location.moveNorth();
            case SOUTH -> location.moveSouth();
            case WEST -> location.moveWest();
            case EAST -> location.moveEast();
            case NORTHEAST -> location.moveNortheast();
            case NORTHWEST -> location.moveNorthwest();
            case SOUTHEAST -> location.moveSoutheast();
            case SOUTHWEST -> location.moveSouthwest();
        })));
    }

    public List<Event<?>> spawnAt(final Coordinates location) {
        if(!Objects.isNull(this.location)) {
            throw new IllegalStateException("Can't spawn twice!");
        }
        if(walls != null && walls.contains(location)) {
            throw new IllegalStateException("Can't spawn on top of a wall!");
        }
        return Collections.singletonList(Event.create(new PlayerMoved(location)));
    }

    public List<Event<?>> buildWallAt(final Coordinates location) {
        if(dimensions == null) {
            throw new IllegalStateException("Can't build a wall before the world has been sized!");
        }
        if(Objects.equals(this.location, location)) {
            throw new IllegalStateException("Can't build a wall on top of the player!");
        }
        if(!dimensions.contains(location)) {
            throw new IllegalStateException("Can't build a wall outside of the world bounds!");
        }

        return Collections.singletonList(Event.create(new WallBuilt(location)));
    }

}
