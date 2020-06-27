package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.DestinationCleared;
import com.atomiccomics.crusoe.player.DestinationUpdated;
import com.atomiccomics.crusoe.world.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Drawer {

    public record Frame(World.Dimensions dimensions,
                        World.Player player,
                        Set<World.Coordinates> walls,
                        Map<World.Coordinates, Item> items,
                        World.Coordinates destination) {

    }

    private volatile World.Dimensions dimensions;
    private volatile World.Player player;
    private volatile World.Coordinates destination;
    private final Set<World.Coordinates> walls = new CopyOnWriteArraySet<>();
    private final Map<World.Coordinates, Item> items = new ConcurrentHashMap<>();

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

    private void handleItemPlaced(final Event<ItemPlaced> event) {
        this.items.put(event.payload().location(), event.payload().item());
    }

    private void handleItemRemoved(final Event<ItemRemoved> event) {
        this.items.remove(event.payload().location());
    }

    private void handleDestinationUpdated(final Event<DestinationUpdated> event) {
        this.destination = event.payload().coordinates();
    }

    private void handleDestinationCleared(final Event<DestinationCleared> event) {
        this.destination = null;
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            switch (event.name().value()) {
                case "WorldResized" -> handleWorldResized((Event<WorldResized>) event);
                case "PlayerMoved" -> handlePlayerMoved((Event<PlayerMoved>)event);
                case "WallBuilt" -> handleWallBuilt((Event<WallBuilt>)event);
                case "WallDestroyed" -> handleWallDestroyed((Event<WallDestroyed>)event);
                case "ItemPlaced" -> handleItemPlaced((Event<ItemPlaced>)event);
                case "ItemRemoved" -> handleItemRemoved((Event<ItemRemoved>)event);
                case "DestinationUpdated" -> handleDestinationUpdated((Event<DestinationUpdated>)event);
                case "DestinationCleared" -> handleDestinationCleared((Event<DestinationCleared>)event);
            };
        }
    }

    public Frame snapshot() {
        return new Frame(dimensions, player, Set.copyOf(walls), Map.copyOf(items), destination);
    }
    
}
