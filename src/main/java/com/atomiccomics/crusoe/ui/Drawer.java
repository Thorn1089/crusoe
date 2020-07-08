package com.atomiccomics.crusoe.ui;

import com.atomiccomics.crusoe.*;
import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.DestinationCleared;
import com.atomiccomics.crusoe.player.DestinationUpdated;
import com.atomiccomics.crusoe.world.*;
import com.google.inject.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Singleton
@RegisteredComponent
public class Drawer {

    public record Frame(World.Dimensions dimensions,
                        boolean isRunning,
                        Collection<Sprite> sprites) {

    }

    private volatile World.Dimensions dimensions;
    private volatile World.Player player;
    private volatile World.Coordinates destination;
    private final Set<World.Coordinates> walls = new CopyOnWriteArraySet<>();
    private final Set<World.Coordinates> blueprints = new CopyOnWriteArraySet<>();
    private final Map<World.Coordinates, Item> items = new ConcurrentHashMap<>();
    private volatile boolean isRunning = false;
    private volatile boolean isPlayerSelected = false;

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

    @Handler(WallBlueprintPlaced.class)
    public void handleWallBlueprintPlaced(final WallBlueprintPlaced event) {
        this.blueprints.add(event.location());
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

    @Handler(PlayerSelected.class)
    public void handlePlayerSelected(final PlayerSelected event) {
        isPlayerSelected = true;
    }

    @Handler(PlayerDeselected.class)
    public void handlePlayerDeselected(final PlayerDeselected event) {
        isPlayerSelected = false;
    }

    public Frame snapshot() {
        final List<Sprite> sprites = new LinkedList<>();
        walls.stream()
                .map(WallSprite::new)
                .forEach(sprites::add);
        blueprints.stream()
                .map(BlueprintSprite::new)
                .forEach(sprites::add);
        Optional.ofNullable(destination).map(DestinationSprite::new).ifPresent(sprites::add);

        sprites.add(new PlayerSprite(player.position(), player.orientation(), isPlayerSelected));

        items.entrySet()
                .stream()
                .map(e -> new ItemSprite(e.getKey(), e.getValue()))
                .forEach(sprites::add);

        return new Frame(dimensions, isRunning, sprites);
    }
    
}
