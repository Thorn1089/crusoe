package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.ItemDropped;
import com.atomiccomics.crusoe.player.PlayerClient;
import com.atomiccomics.crusoe.world.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Picker {

    private final PlayerClient playerClient;
    private final WorldClient worldClient;

    private final Map<World.Coordinates, Item> items = new ConcurrentHashMap<>();
    private volatile World.Coordinates player;

    public Picker(final PlayerClient playerClient, final WorldClient worldClient) {
        this.playerClient = playerClient;
        this.worldClient = worldClient;
    }

    private void handleItemPlaced(final Event<ItemPlaced> event) {
        this.items.put(event.payload().location(), event.payload().item());
    }

    private void handleItemRemoved(final Event<ItemRemoved> event) {
        this.items.remove(event.payload().location(), event.payload().item());
    }

    private void handlePlayerMoved(final Event<PlayerMoved> event) {
        if(Objects.equals(this.player, event.payload().player().position())) {
            // Don't count turns; otherwise turning after a drop picks the item back up
            return;
        }
        this.player = event.payload().player().position();
        if(items.containsKey(player)) {
            final var item = items.get(player);
            playerClient.update(p -> p.pickUpItem(item));
            worldClient.update(w -> w.removeItemAt(item, player));
        }
    }

    private void handleItemDropped(final Event<ItemDropped> event) {
        worldClient.update(w -> w.spawnItemAt(event.payload().item(), player));
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            switch (event.name().value()) {
                case "ItemPlaced" -> handleItemPlaced((Event<ItemPlaced>)event);
                case "ItemRemoved" -> handleItemRemoved((Event<ItemRemoved>)event);
                case "PlayerMoved" -> handlePlayerMoved((Event<PlayerMoved>)event);
                case "ItemDropped" -> handleItemDropped((Event<ItemDropped>)event);
            }
        }
    }

}
