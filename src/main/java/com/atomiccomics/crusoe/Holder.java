package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.ItemDropped;
import com.atomiccomics.crusoe.player.ItemPickedUp;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class Holder {

    private final Set<Item> inventory = new CopyOnWriteArraySet<>();

    private void handleItemPickedUp(final Event<ItemPickedUp> event) {
        inventory.add(event.payload().item());
    }

    private void handleItemDropped(final Event<ItemDropped> event) {
        inventory.add(event.payload().item());
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            switch(event.name().value()) {
                case "ItemPickedUp" -> handleItemPickedUp((Event<ItemPickedUp>)event);
                case "ItemDropped" -> handleItemDropped((Event<ItemDropped>)event);
            }
        }
    }

    public boolean hasItems() {
        return !inventory.isEmpty();
    }

}
