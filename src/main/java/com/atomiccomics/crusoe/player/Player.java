package com.atomiccomics.crusoe.player;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.world.World;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public final class Player {

    public static final class PlayerState {
        private final Set<Item> inventory = new CopyOnWriteArraySet<>();
        private World.Coordinates latestDestination;

        public PlayerState handleItemPickedUp(final Event<ItemPickedUp> event) {
            this.inventory.add(event.payload().item());
            return this;
        }

        public PlayerState handleItemDropped(final Event<ItemDropped> event) {
            this.inventory.remove(event.payload().item());
            return this;
        }

        public PlayerState handleDestinationUpdated(final Event<DestinationUpdated> event) {
            latestDestination = event.payload().coordinates();
            return this;
        }

        public PlayerState handleDestinationCleared(final Event<DestinationCleared> event) {
            latestDestination = null;
            return this;
        }

        public PlayerState process(final List<Event<?>> batch) {
            for(final var event : batch) {
                switch(event.name().value()) {
                    case "ItemPickedUp" -> handleItemPickedUp((Event<ItemPickedUp>)event);
                    case "ItemDropped" -> handleItemDropped((Event<ItemDropped>)event);
                    case "DestinationUpdated" -> handleDestinationUpdated((Event<DestinationUpdated>)event);
                    case "DestinationCleared" -> handleDestinationCleared((Event<DestinationCleared>)event);
                }
            }
            return this;
        }

        public Set<Item> inventory() {
            return new HashSet<>(inventory);
        }

        public World.Coordinates destination() {
            return latestDestination;
        }
    }

    private final Set<Item> inventory;
    private final World.Coordinates latestDestination;

    public Player(final PlayerState state) {
        this.inventory = state.inventory();
        this.latestDestination = state.destination();
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

    public List<Event<?>> setDestination(final World.Coordinates coordinates) {
        if(latestDestination != null) {
            return Arrays.asList(Event.create(new DestinationCleared()), Event.create(new DestinationUpdated(coordinates)));
        }
        return Collections.singletonList(Event.create(new DestinationUpdated(coordinates)));
    }

    public List<Event<?>> clearDestination() {
        return Collections.singletonList(Event.create(new DestinationCleared()));
    }

}
