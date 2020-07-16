package com.atomiccomics.crusoe.player;

import com.atomiccomics.crusoe.Handler;
import com.atomiccomics.crusoe.RegisteredComponent;
import com.atomiccomics.crusoe.graph.ImpossiblePathException;
import com.atomiccomics.crusoe.time.RepeatingTask;
import com.atomiccomics.crusoe.time.Schedule;
import com.atomiccomics.crusoe.time.Scheduler;
import com.atomiccomics.crusoe.world.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayDeque;

@Singleton
@RegisteredComponent
public final class Navigator {

    private final Grapher grapher;
    private final WorldClient worldClient;
    private final PlayerClient playerClient;
    private final Scheduler scheduler;

    private volatile World.Player player;
    private volatile Schedule runningTask;

    @Inject
    public Navigator(final Grapher grapher, final WorldClient worldClient, final PlayerClient playerClient, final Scheduler scheduler) {
        this.grapher = grapher;
        this.worldClient = worldClient;
        this.playerClient = playerClient;
        this.scheduler = scheduler;
    }

    @Handler(PlayerMoved.class)
    public void handlePlayerMoved(final PlayerMoved event) {
        this.player = event.player();
    }

    @Handler(DestinationUpdated.class)
    public void handleDestinationUpdated(final DestinationUpdated event) {
        if(player == null) {
            //Player hasn't spawned yet!
            return;
        }
        try {
            final var path = new ArrayDeque<>(grapher.findPathBetween(player.position(), event.coordinates()));
            final RepeatingTask task = () -> {
                final var step = path.remove();
                worldClient.update(w -> w.move(step));
                if(path.isEmpty()) {
                    playerClient.update(Player::clearDestination);
                }
                return path.isEmpty();
            };
            runningTask = scheduler.scheduleRepeatingTask(task);
            //TODO Recalculate if path invalidated, e.g. world resized or wall built/destroyed
        } catch (final ImpossiblePathException e) {
            //Unable to actually reach the new destination, so don't bother
            //TODO Signal that the goal is impossible somehow
        }
    }

    @Handler(DestinationCleared.class)
    public void handleDestinationCleared(final DestinationCleared event) {
        runningTask.cancel();
        runningTask = null;
    }

    public boolean isLegalDestination(final World.Coordinates destination) {
        return grapher.isLegalDestination(destination);
    }

}
