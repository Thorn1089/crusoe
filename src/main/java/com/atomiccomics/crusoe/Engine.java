package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.player.Player;
import com.atomiccomics.crusoe.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class Engine {

    private final List<Component> components = new LinkedList<>();

    private final World.WorldState worldState = new World.WorldState();
    private final Player.PlayerState playerState = new Player.PlayerState();

    public void register(final Component component) {
        components.add(component);
    }

    public void updateWorld(final Function<World, List<Event<?>>> updater) {
        final var batch = updater.apply(new World(worldState));
        worldState.process(batch);
        for(final var component : components) {
            component.process(batch);
        }
    }

    public void updatePlayer(final Function<Player, List<Event<?>>> updater) {
        final var batch = updater.apply(new Player(playerState));
        playerState.process(batch);
        for(final var component : components) {
            component.process(batch);
        }
    }

}
