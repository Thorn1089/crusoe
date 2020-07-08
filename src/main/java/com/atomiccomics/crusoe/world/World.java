package com.atomiccomics.crusoe.world;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.item.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

        public static long manhattanDistance(final Coordinates first, final Coordinates second) {
            return Math.abs(first.x - second.x) + Math.abs(first.y - second.y);
        }

        public Coordinates {
            if(x < 0) {
                throw new IllegalArgumentException("X coordinate cannot be negative");
            }
            if(y < 0) {
                throw new IllegalArgumentException("Y coordinate cannot be negative");
            }
        }

        public Direction to(final Coordinates destination) {
            final var xDiff = destination.x - x;
            final var yDiff = destination.y - y;

            if(xDiff > 0 && yDiff > 0) {
                return Direction.NORTHEAST;
            } else if(xDiff > 0 && yDiff < 0) {
                return Direction.SOUTHEAST;
            } else if(xDiff > 0) {
                return Direction.EAST;
            } else if(xDiff < 0 && yDiff > 0) {
                return Direction.NORTHWEST;
            } else if(xDiff < 0 && yDiff < 0) {
                return Direction.SOUTHWEST;
            } else if(xDiff < 0) {
                return Direction.WEST;
            } else if(yDiff > 0) {
                return Direction.NORTH;
            } else if (yDiff < 0) {
                return Direction.SOUTH;
            }
            throw new IllegalArgumentException("Destination coordinates are the same as origin coordinates");
        }

        public Coordinates moveTowards(final Direction direction) {
            return switch (direction) {
                case NORTH -> moveNorth();
                case SOUTH -> moveSouth();
                case WEST -> moveWest();
                case EAST -> moveEast();
                case NORTHEAST -> moveNortheast();
                case NORTHWEST -> moveNorthwest();
                case SOUTHEAST -> moveSoutheast();
                case SOUTHWEST -> moveSouthwest();
            };
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

        public boolean isSmaller(final Dimensions other) {
            return other.width() > this.width() || other.height() > this.height();
        }
    }

    public record Player(Coordinates position, Direction orientation) {
        public Coordinates lookingAt() {
            return position.moveTowards(orientation);
        }
    }

    public static final class WorldState {
        private volatile Dimensions dimensions;
        private volatile Player player;
        private final Set<Coordinates> walls = new CopyOnWriteArraySet<>();
        private final Set<Coordinates> blueprints = new CopyOnWriteArraySet<>();
        private final Map<Coordinates, Item> items = new ConcurrentHashMap<>();

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

        public WorldState handleWallDestroyed(final Event<WallDestroyed> event) {
            this.walls.remove(event.payload().location());
            return this;
        }

        public WorldState handleWallBlueprintPlaced(final Event<WallBlueprintPlaced> event) {
            this.blueprints.add(event.payload().location());
            return this;
        }

        public WorldState handleItemPlaced(final Event<ItemPlaced> event) {
            this.items.put(event.payload().location(), event.payload().item());
            return this;
        }

        public WorldState handleItemRemoved(final Event<ItemRemoved> event) {
            this.items.remove(event.payload().location());
            return this;
        }

        public WorldState process(final List<Event<?>> batch) {
            WorldState updatedState = this;
            for(final var event : batch) {
                updatedState = switch (event.name().value()) {
                    case "WorldResized" -> updatedState.handleWorldResized((Event<WorldResized>) event);
                    case "PlayerMoved" -> updatedState.handlePlayerMoved((Event<PlayerMoved>)event);
                    case "WallBuilt" -> updatedState.handleWallBuilt((Event<WallBuilt>)event);
                    case "WallDestroyed" -> updatedState.handleWallDestroyed((Event<WallDestroyed>)event);
                    case "WallBlueprintPlaced" -> updatedState.handleWallBlueprintPlaced((Event<WallBlueprintPlaced>)event);
                    case "ItemPlaced" -> updatedState.handleItemPlaced((Event<ItemPlaced>)event);
                    case "ItemRemoved" -> updatedState.handleItemRemoved((Event<ItemRemoved>)event);
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

        public Set<Coordinates> blueprints() {
            return new HashSet<>(blueprints);
        }

        public Map<Coordinates, Item> items() {
            return new HashMap<>(items);
        }
    }

    private final Dimensions dimensions;
    private final Player player;
    private final Set<Coordinates> walls;
    private final Set<Coordinates> blueprints;
    private final Map<Coordinates, Item> items;

    public World(final WorldState state) {
        this.dimensions = state.dimensions();
        this.player = state.player();
        this.walls = state.walls();
        this.blueprints = state.blueprints();
        this.items = state.items();
    }

    public List<Event<?>> resize(final Dimensions dimensions) {
        if(Objects.equals(dimensions, this.dimensions)) {
            return Collections.emptyList();
        }

        if(this.dimensions == null || !dimensions.isSmaller(this.dimensions)) {
            return Collections.singletonList(Event.create(new WorldResized(dimensions)));
        }

        final var updates = new LinkedList<Event<?>>();

        if(!Objects.isNull(player)) {
            final var newX = Math.min(player.position().x(), dimensions.width() - 1);
            final var newY = Math.min(player.position().y(), dimensions.height() - 1);
            final var newLocation = new Coordinates(newX, newY);

            if(!Objects.equals(player.position(), newLocation)) {
                updates.add(Event.create(new PlayerMoved(new Player(newLocation, player.orientation()))));
            }
        }

        walls.stream()
                .filter(w -> !dimensions.contains(w))
                .map(w -> Event.create(new WallDestroyed(w)))
                .forEach(updates::add);

        updates.add(Event.create(new WorldResized(dimensions)));

        return updates;
    }

    public List<Event<?>> turn(final Direction direction) {
        assertPlayerSpawned();

        if(player.orientation() == direction) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Event.create(new PlayerMoved(new Player(player.position(), direction))));
    }

    public List<Event<?>> move(final Direction direction) {
        assertPlayerSpawned();

        if (!direction.isLegal(dimensions, player.position(), walls)) {
            throw new IllegalStateException("Can't move into this spot; moving " + direction + " from " + player.position() + " within a world of size " + dimensions);
        }

        return Collections.singletonList(Event.create(new PlayerMoved(new Player(player.position().moveTowards(direction), direction))));
    }

    public List<Event<?>> spawnPlayerAt(final Coordinates location) {
        assertPlayerNotSpawned();

        if(walls != null && walls.contains(location)) {
            throw new IllegalStateException("Can't spawn on top of a wall!");
        }
        return Collections.singletonList(Event.create(new PlayerMoved(new Player(location, Direction.NORTH))));
    }

    public List<Event<?>> spawnItemAt(final Item item, final Coordinates location) {
        assertDimensionsProvided();
        assertDimensionsContainLocation(location);
        assertWallNotAtLocation(location);

        if(Objects.equals(item, items.get(location))) {
            return Collections.emptyList();
        }

        return Collections.singletonList(Event.create(new ItemPlaced(item, location)));
    }

    public List<Event<?>> removeItemAt(final Item item, final Coordinates location) {
        assertDimensionsProvided();
        assertDimensionsContainLocation(location);

        if(!items.containsKey(location)) {
            return Collections.emptyList();
        }
        if(!Objects.equals(item, items.get(location))) {
            throw new IllegalStateException("Trying to remove the wrong item from this location!");
        }
        return Collections.singletonList(Event.create(new ItemRemoved(item, location)));
    }

    public List<Event<?>> buildWallAt(final Coordinates location) {
        assertDimensionsProvided();
        assertDimensionsContainLocation(location);
        assertPlayerNotAtLocation(location);
        assertItemNotAtLocation(location);

        if(walls.contains(location)) {
            return Collections.emptyList();
        }

        return Collections.singletonList(Event.create(new WallBuilt(location)));
    }

    public List<Event<?>> destroyWallAt(final Coordinates location) {
        assertDimensionsProvided();
        assertDimensionsContainLocation(location);
        assertPlayerNotAtLocation(location);

        if(!walls.contains(location)) {
            return Collections.emptyList();
        }

        return Collections.singletonList(Event.create(new WallDestroyed(location)));
    }

    public List<Event<?>> placeWallBlueprintAt(final Coordinates location) {
        assertDimensionsProvided();
        assertDimensionsContainLocation(location);
        assertLocationEmpty(location);

        if(blueprints.contains(location)) {
            return Collections.emptyList();
        }

        return Collections.singletonList(Event.create(new WallBlueprintPlaced(location)));
    }

    private void assertDimensionsProvided() {
        if(dimensions == null) {
            throw new IllegalStateException("Can't build a wall before the world has been sized!");
        }
    }

    private void assertDimensionsContainLocation(final Coordinates location) {
        if(!dimensions.contains(location)) {
            throw new IllegalStateException("Can't build a wall outside of the world bounds!");
        }
    }

    private void assertPlayerNotAtLocation(final Coordinates location) {
        if(Optional.ofNullable(player).map(Player::position).map(location::equals).orElse(false)) {
            throw new IllegalStateException("Can't build a wall on top of the player!");
        }
    }

    private void assertWallNotAtLocation(final Coordinates location) {
        if(walls.contains(location)) {
            throw new IllegalStateException("Can't spawn an item inside of a wall!");
        }
    }

    private void assertItemNotAtLocation(final Coordinates location) {
        if(items.containsKey(location)) {
            throw new IllegalStateException("Can't build a wall on top of an item!");
        }
    }

    private void assertLocationEmpty(final Coordinates location) {
        assertWallNotAtLocation(location);
        assertItemNotAtLocation(location);
        assertPlayerNotAtLocation(location);
    }

    private void assertPlayerSpawned() {
        if(player == null) {
            throw new IllegalStateException("Player has not been spawned yet!");
        }
    }

    private void assertPlayerNotSpawned() {
        if(player != null) {
            throw new IllegalStateException("Player has already been spawned!");
        }
    }

}
