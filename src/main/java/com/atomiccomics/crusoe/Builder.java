package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.ItemDropped;
import com.atomiccomics.crusoe.player.ItemPickedUp;
import com.atomiccomics.crusoe.world.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Builder {

    private volatile World.Dimensions dimensions;
    private final Set<World.Coordinates> walls = new HashSet<>();
    private volatile World.Player player;
    private volatile boolean hasPickaxe = false;

    private void handleWorldResized(final Event<WorldResized> event) {
        this.dimensions = event.payload().dimensions();
    }

    private void handleWallBuilt(final Event<WallBuilt> event) {
        this.walls.add(event.payload().location());
    }

    private void handleWallDestroyed(final Event<WallDestroyed> event) {
        this.walls.remove(event.payload().location());
    }

    private void handlePlayerMoved(final Event<PlayerMoved> event) {
        this.player = event.payload().player();
    }

    private void handleItemPickedUp(final Event<ItemPickedUp> event) {
        if(event.payload().item() == Item.PICKAXE) {
            hasPickaxe = true;
        }
    }

    private void handleItemDropped(final Event<ItemDropped> event) {
        if(event.payload().item() == Item.PICKAXE) {
            hasPickaxe = false;
        }
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            switch (event.name().value()) {
                case "WorldResized" -> handleWorldResized((Event<WorldResized>) event);
                case "WallBuilt" -> handleWallBuilt((Event<WallBuilt>)event);
                case "WallDestroyed" -> handleWallDestroyed((Event<WallDestroyed>)event);
                case "PlayerMoved" -> handlePlayerMoved((Event<PlayerMoved>)event);
                case "ItemPickedUp" -> handleItemPickedUp((Event<ItemPickedUp>)event);
                case "ItemDropped" -> handleItemDropped((Event<ItemDropped>)event);
            };
        }
    }

    public boolean canBuildWherePlayerLooking() {
        if(player == null) {
            //Player hasn't spawned yet!
            return false;
        }
        if(dimensions == null) {
            //The world hasn't been sized yet!
            return false;
        }

        final World.Coordinates location = player.lookingAt();
        return hasPickaxe && dimensions.contains(location) && !walls.contains(location);
    }

    public boolean canDestroyWherePlayerLooking() {
        if(player == null) {
            //Player hasn't spawned yet!
            return false;
        }
        if(dimensions == null) {
            //The world hasn't been sized yet!
            return false;
        }

        final World.Coordinates location = player.lookingAt();
        return hasPickaxe && dimensions.contains(location) && walls.contains(location);
    }

    public World.Coordinates playerTarget() {
        if(player == null) {
            throw new IllegalStateException("Cannot request the player target before they spawn!");
        }
        return player.lookingAt();
    }

}
