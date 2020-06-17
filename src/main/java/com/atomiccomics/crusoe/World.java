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

        public Coordinates moveUp() {
            return new Coordinates(x, y + 1);
        }

        public Coordinates moveDown() {
            return new Coordinates(x, y - 1);
        }

        public Coordinates moveLeft() {
            return new Coordinates(x - 1, y);
        }

        public Coordinates moveRight() {
            return new Coordinates(x + 1, y);
        }
    }

    public record Width(int size) {
        public Width {
            if(size <= 0) {
                throw new IllegalArgumentException("Width must be positive and non-zero");
            }
        }
    }

    public record Height(int size) {
        public Height {
            if(size <= 0) {
                throw new IllegalArgumentException("Height must be positive and non-zero");
            }
        }
    }

    public static final class WorldState {
        private volatile Width width;
        private volatile Height height;
        private volatile Coordinates location;

        public WorldState handleWorldResized(final Event<WorldResized> event) {
            this.width = event.payload().width();
            this.height = event.payload().height();
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

        public Width width() {
            return width;
        }

        public Height height() {
            return height;
        }

        public Coordinates location() {
            return location;
        }
    }

    public enum Direction {
        UP {
            @Override
            public boolean isLegal(Width width, Height height, Coordinates location) {
                return location.y() < height.size() - 1;
            }
        }, DOWN {
            @Override
            public boolean isLegal(Width width, Height height, Coordinates location) {
                return location.y() > 0;
            }
        }, LEFT {
            @Override
            public boolean isLegal(Width width, Height height, Coordinates location) {
                return location.x() > 0;
            }
        }, RIGHT {
            @Override
            public boolean isLegal(Width width, Height height, Coordinates location) {
                return location.x() < width.size() - 1;
            }
        };

        public abstract boolean isLegal(Width width, Height height, Coordinates location);
    }

    private final Width width;
    private final Height height;
    private final Coordinates location;

    public World(final WorldState state) {
        this.width = state.width;
        this.height = state.height;
        this.location = state.location;
    }

    public List<Event<?>> resize(final Width width, final Height height) {
        if(Objects.equals(width, this.width) && Objects.equals(height, this.height)) {
            return Collections.emptyList();
        }

        if(this.width == null && this.height == null) {
            return Collections.singletonList(Event.create(new WorldResized(width, height)));
        }

        if(width.size() < this.width.size() || height.size() < this.height.size()) {
            final var newX = Math.min(location.x(), width.size() - 1);
            final var newY = Math.min(location.y(), height.size() - 1);

            return Arrays.asList(
                    Event.create(new PlayerMoved(new Coordinates(newX, newY))),
                    Event.create(new WorldResized(width, height)));
        }

        return Collections.singletonList(Event.create(new WorldResized(width, height)));
    }

    public List<Event<?>> move(final Direction direction) {
        if (!direction.isLegal(width, height, location)) {
            throw new IllegalStateException("Already at edge of world!");
        }

        return switch (direction) {
            case UP -> Collections.singletonList(Event.create(new PlayerMoved(location.moveUp())));
            case DOWN -> Collections.singletonList(Event.create(new PlayerMoved(location.moveDown())));
            case LEFT -> Collections.singletonList(Event.create(new PlayerMoved(location.moveLeft())));
            case RIGHT -> Collections.singletonList(Event.create(new PlayerMoved(location.moveRight())));
        };
    }

    public List<Event<?>> spawnAt(final Coordinates location) {
        if(!Objects.isNull(this.location)) {
            throw new IllegalStateException("Can't spawn twice!");
        }
        return Collections.singletonList(Event.create(new PlayerMoved(location)));
    }

}
