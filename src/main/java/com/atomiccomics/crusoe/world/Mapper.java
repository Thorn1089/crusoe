package com.atomiccomics.crusoe.world;

import com.atomiccomics.crusoe.Handler;
import com.atomiccomics.crusoe.RegisteredComponent;
import com.atomiccomics.crusoe.item.Item;
import com.google.inject.Singleton;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RegisteredComponent
public class Mapper {

    private volatile World.Coordinates player;
    private final Map<Item, World.Coordinates> items = new ConcurrentHashMap<>();

    @Handler(PlayerMoved.class)
    public void handlePlayerMoved(final PlayerMoved event) {
        player = event.player().position();
    }

    @Handler(ItemPlaced.class)
    public void handleItemPlaced(final ItemPlaced event) {
        items.put(event.item(), event.location());
    }

    @Handler(ItemRemoved.class)
    public void handleItemRemoved(final ItemRemoved event) {
        items.remove(event.item());
    }

    public Optional<World.Coordinates> playerLocation() {
        return Optional.ofNullable(player);
    }

    public Optional<World.Coordinates> itemLocation(final Item item) {
        return Optional.ofNullable(items.get(item));
    }

}
