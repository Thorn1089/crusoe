package com.atomiccomics.crusoe.player;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.item.Item;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public final class Player {

    public static final class PlayerState {
        private final Set<Item> inventory = new CopyOnWriteArraySet<>();

        public PlayerState handleItemPickedUp(final Event<ItemPickedUp> event) {
            this.inventory.add(event.payload().item());
            return this;
        }

        public PlayerState handleItemDropped(final Event<ItemDropped> event) {
            this.inventory.remove(event.payload().item());
            return this;
        }

        public PlayerState process(final List<Event<?>> batch) {
            for(final var event : batch) {
                switch(event.name().value()) {
                    case "ItemPickedUp" -> handleItemPickedUp((Event<ItemPickedUp>)event);
                    case "ItemDropped" -> handleItemDropped((Event<ItemDropped>)event);
                }
            }
            return this;
        }

        public Set<Item> inventory() {
            return new HashSet<>(inventory);
        }
    }

    private final Set<Item> inventory;

    public Player(final PlayerState state) {
        this.inventory = state.inventory();
    }

    public List<Event<?>> pickUpItem(final Item item) {
        if(inventory.contains(item)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Event.create(new ItemPickedUp(item)));
    }

    public List<Event<?>> dropItem(final Item item) {
        if(!inventory.contains(item)) {
            return Collections.emptyList();
        }

        return Collections.singletonList(Event.create(new ItemDropped(item)));
    }

    //TODO Migrate commands for build/destroy walls?
}
