package com.atomiccomics.crusoe.ui;

import com.atomiccomics.crusoe.GamePaused;
import com.atomiccomics.crusoe.GameResumed;
import com.atomiccomics.crusoe.Handler;
import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.DestinationCleared;
import com.atomiccomics.crusoe.player.DestinationUpdated;
import com.atomiccomics.crusoe.world.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Drawer {

    public record Frame(World.Dimensions dimensions,
                        World.Player player,
                        Set<World.Coordinates> walls,
                        Map<World.Coordinates, Item> items,
                        World.Coordinates destination,
                        boolean isRunning) {

    }

    private volatile World.Dimensions dimensions;
    private volatile World.Player player;
    private volatile World.Coordinates destination;
    private final Set<World.Coordinates> walls = new CopyOnWriteArraySet<>();
    private final Map<World.Coordinates, Item> items = new ConcurrentHashMap<>();
    private volatile boolean isRunning = false;

    @Handler(WorldResized.class)
    public void handleWorldResized(final WorldResized event) {
        this.dimensions = event.dimensions();
    }

    @Handler(PlayerMoved.class)
    public void handlePlayerMoved(final PlayerMoved event) {
        this.player = event.player();
    }

    @Handler(WallBuilt.class)
    public void handleWallBuilt(final WallBuilt event) {
        this.walls.add(event.location());
    }

    @Handler(WallDestroyed.class)
    public void handleWallDestroyed(final WallDestroyed event) {
        this.walls.remove(event.location());
    }

    @Handler(ItemPlaced.class)
    public void handleItemPlaced(final ItemPlaced event) {
        this.items.put(event.location(), event.item());
    }

    @Handler(ItemRemoved.class)
    public void handleItemRemoved(final ItemRemoved event) {
        this.items.remove(event.location());
    }

    @Handler(DestinationUpdated.class)
    public void handleDestinationUpdated(final DestinationUpdated event) {
        this.destination = event.coordinates();
    }

    @Handler(DestinationCleared.class)
    public void handleDestinationCleared(final DestinationCleared event) {
        this.destination = null;
    }

    @Handler(GamePaused.class)
    public void handleGamePaused(final GamePaused event) {
        isRunning = false;
    }

    @Handler(GameResumed.class)
    public void handleGameResumed(final GameResumed event) {
        isRunning = true;
    }

    public Frame snapshot() {
        return new Frame(dimensions, player, Set.copyOf(walls), Map.copyOf(items), destination, isRunning);
    }
    
}
