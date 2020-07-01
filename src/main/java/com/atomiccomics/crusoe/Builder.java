package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.ItemDropped;
import com.atomiccomics.crusoe.player.ItemPickedUp;
import com.atomiccomics.crusoe.world.*;
import com.google.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

@Singleton
@RegisteredComponent
public final class Builder {

    private volatile World.Dimensions dimensions;
    private final Set<World.Coordinates> walls = new HashSet<>();
    private volatile World.Player player;
    private volatile boolean hasPickaxe = false;

    @Handler(WorldResized.class)
    public void handleWorldResized(final WorldResized event) {
        this.dimensions = event.dimensions();
    }

    @Handler(WallBuilt.class)
    public void handleWallBuilt(final WallBuilt event) {
        this.walls.add(event.location());
    }

    @Handler(WallDestroyed.class)
    public void handleWallDestroyed(final WallDestroyed event) {
        this.walls.remove(event.location());
    }

    @Handler(PlayerMoved.class)
    public void handlePlayerMoved(final PlayerMoved event) {
        this.player = event.player();
    }

    @Handler(ItemPickedUp.class)
    public void handleItemPickedUp(final ItemPickedUp event) {
        if(event.item() == Item.PICKAXE) {
            hasPickaxe = true;
        }
    }

    @Handler(ItemDropped.class)
    public void handleItemDropped(final ItemDropped event) {
        if(event.item() == Item.PICKAXE) {
            hasPickaxe = false;
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
