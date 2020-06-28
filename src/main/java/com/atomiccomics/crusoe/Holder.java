package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.ItemDropped;
import com.atomiccomics.crusoe.player.ItemPickedUp;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class Holder {

    private final Set<Item> inventory = new CopyOnWriteArraySet<>();

    @Handler(ItemPickedUp.class)
    public void handleItemPickedUp(final ItemPickedUp event) {
        inventory.add(event.item());
    }

    @Handler(ItemDropped.class)
    public void handleItemDropped(final ItemDropped event) {
        inventory.add(event.item());
    }

    public boolean hasItems() {
        return !inventory.isEmpty();
    }

}
