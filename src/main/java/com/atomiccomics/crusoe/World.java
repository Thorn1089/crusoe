package com.atomiccomics.crusoe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    }

    public static final class WorldState {
        private volatile Dimensions dimensions;
        private volatile Coordinates location;

        public WorldState handleWorldResized(final Event<WorldResized> event) {
            this.dimensions = event.payload().dimensions();
            return this;
        }

        public WorldState handlePlayerMoved(final Event<PlayerMoved> event) {
            this.location = event.payload().location();
            return this;
        }

        public WorldState process(final List<Event<?>> batch) {
            WorldState updatedState = this;
            for(final var event : batch) {
                updatedState = switch (event.name().value()) {
                    case "WorldResized" -> updatedState.handleWorldResized((Event<WorldResized>) event);
                    case "PlayerMoved" -> updatedState.handlePlayerMoved((Event<PlayerMoved>)event);
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
    }

    public enum Direction {
        NORTH {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location) {
                return location.y() < dimensions.height() - 1;
            }
        }, SOUTH {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location) {
                return location.y() > 0;
            }
        }, WEST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location) {
                return location.x() > 0;
            }
        }, EAST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location) {
                return location.x() < dimensions.width() - 1;
            }
        }, NORTHEAST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location) {
                return NORTH.isLegal(dimensions, location) && EAST.isLegal(dimensions, location);
            }
        }, SOUTHEAST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location) {
                return SOUTH.isLegal(dimensions, location) && EAST.isLegal(dimensions, location);
            }
        }, NORTHWEST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location) {
                return NORTH.isLegal(dimensions, location) && WEST.isLegal(dimensions, location);
            }
        }, SOUTHWEST {
            @Override
            public boolean isLegal(Dimensions dimensions, Coordinates location) {
                return SOUTH.isLegal(dimensions, location) && WEST.isLegal(dimensions, location);
            }
        };

        public abstract boolean isLegal(Dimensions dimensions, Coordinates location);
    }

    private final Dimensions dimensions;
    private final Coordinates location;

    public World(final WorldState state) {
        this.dimensions = state.dimensions();
        this.location = state.location();
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
        if (!direction.isLegal(dimensions, location)) {
            throw new IllegalStateException("Already at edge of world!");
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
        return Collections.singletonList(Event.create(new PlayerMoved(location)));
    }

}
