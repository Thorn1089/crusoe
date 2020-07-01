package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.ItemDropped;
import com.atomiccomics.crusoe.player.PlayerClient;
import com.atomiccomics.crusoe.world.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RegisteredComponent
public final class Picker {

    private final PlayerClient playerClient;
    private final WorldClient worldClient;

    private final Map<World.Coordinates, Item> items = new ConcurrentHashMap<>();
    private volatile World.Coordinates player;

    @Inject
    public Picker(final PlayerClient playerClient, final WorldClient worldClient) {
        this.playerClient = playerClient;
        this.worldClient = worldClient;
    }

    @Handler(ItemPlaced.class)
    public void handleItemPlaced(final ItemPlaced event) {
        this.items.put(event.location(), event.item());
    }

    @Handler(ItemRemoved.class)
    public void handleItemRemoved(final ItemRemoved event) {
        this.items.remove(event.location(), event.item());
    }

    @Handler(PlayerMoved.class)
    public void handlePlayerMoved(final PlayerMoved event) {
        if(Objects.equals(this.player, event.player().position())) {
            // Don't count turns; otherwise turning after a drop picks the item back up
            return;
        }
        this.player = event.player().position();
        if(items.containsKey(player)) {
            final var item = items.get(player);
            playerClient.update(p -> p.pickUpItem(item));
            worldClient.update(w -> w.removeItemAt(item, player));
        }
    }

    @Handler(ItemDropped.class)
    public void handleItemDropped(final ItemDropped event) {
        worldClient.update(w -> w.spawnItemAt(event.item(), player));
    }

}
