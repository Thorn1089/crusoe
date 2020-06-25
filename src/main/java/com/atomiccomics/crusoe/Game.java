package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.player.Player;
import com.atomiccomics.crusoe.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Game {

    private final List<Consumer<List<Event<?>>>> eventListeners = new LinkedList<>();

    private final World.WorldState worldState = new World.WorldState();
    private final Player.PlayerState playerState = new Player.PlayerState();

    public void register(final Consumer<List<Event<?>>> listener) {
        eventListeners.add(listener);
    }

    public void updateWorld(final Function<World, List<Event<?>>> updater) {
        final var batch = updater.apply(new World(worldState));
        worldState.process(batch);
        for(final var listener : eventListeners) {
            listener.accept(batch);
        }
    }

    public void updatePlayer(final Function<Player, List<Event<?>>> updater) {
        final var batch = updater.apply(new Player(playerState));
        playerState.process(batch);
        for(final var listener : eventListeners) {
            listener.accept(batch);
        }
    }

}
