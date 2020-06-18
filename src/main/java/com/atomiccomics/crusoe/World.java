package com.atomiccomics.crusoe;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public final class World {

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

    public record Player(Coordinates position, Direction orientation) {

    }

    public static final class WorldState {
        private volatile Dimensions dimensions;
        private volatile Player player;
        private final Set<Coordinates> walls = new CopyOnWriteArraySet<>();

        public WorldState handleWorldResized(final Event<WorldResized> event) {
            this.dimensions = event.payload().dimensions();
            return this;
        }

        public WorldState handlePlayerMoved(final Event<PlayerMoved> event) {
            this.player = event.payload().player();
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

        public Player player() {
            return player;
        }

        public Set<Coordinates> walls() {
            return new HashSet<>(walls);
        }
    }

    private final Dimensions dimensions;
    private final Player player;
    private final Set<Coordinates> walls;

    public World(final WorldState state) {
        this.dimensions = state.dimensions();
        this.player = state.player();
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
            final var newX = Math.min(player.position().x(), dimensions.width() - 1);
            final var newY = Math.min(player.position().y(), dimensions.height() - 1);
            final var newLocation = new Coordinates(newX, newY);

            if(!Objects.equals(player.position(), newLocation)) {
                return Arrays.asList(
                        Event.create(new PlayerMoved(new Player(newLocation, player.orientation()))),
                        Event.create(new WorldResized(dimensions)));
            }
        }

        return Collections.singletonList(Event.create(new WorldResized(dimensions)));
    }

    public List<Event<?>> move(final Direction direction) {
        if (!direction.isLegal(dimensions, player.position(), walls)) {
            throw new IllegalStateException("Can't move into this spot!");
        }

        return Collections.singletonList(Event.create(new PlayerMoved(new Player(switch (direction) {
            case NORTH -> player.position().moveNorth();
            case SOUTH -> player.position().moveSouth();
            case WEST -> player.position().moveWest();
            case EAST -> player.position().moveEast();
            case NORTHEAST -> player.position().moveNortheast();
            case NORTHWEST -> player.position().moveNorthwest();
            case SOUTHEAST -> player.position().moveSoutheast();
            case SOUTHWEST -> player.position().moveSouthwest();
        }, direction))));
    }

    public List<Event<?>> spawnAt(final Coordinates location) {
        if(!Objects.isNull(this.player)) {
            throw new IllegalStateException("Can't spawn twice!");
        }
        if(walls != null && walls.contains(location)) {
            throw new IllegalStateException("Can't spawn on top of a wall!");
        }
        return Collections.singletonList(Event.create(new PlayerMoved(new Player(location, Direction.NORTH))));
    }

    public List<Event<?>> buildWallAt(final Coordinates location) {
        if(dimensions == null) {
            throw new IllegalStateException("Can't build a wall before the world has been sized!");
        }
        if(Optional.ofNullable(player).map(Player::position).map(location::equals).orElse(false)) {
            throw new IllegalStateException("Can't build a wall on top of the player!");
        }
        if(!dimensions.contains(location)) {
            throw new IllegalStateException("Can't build a wall outside of the world bounds!");
        }

        return Collections.singletonList(Event.create(new WallBuilt(location)));
    }

}
