package com.atomiccomics.crusoe.player;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.time.RepeatingTask;
import com.atomiccomics.crusoe.time.Schedule;
import com.atomiccomics.crusoe.time.Scheduler;
import com.atomiccomics.crusoe.world.Grapher;
import com.atomiccomics.crusoe.world.PlayerMoved;
import com.atomiccomics.crusoe.world.World;
import com.atomiccomics.crusoe.world.WorldClient;

import java.util.ArrayDeque;
import java.util.List;

public final class Navigator {

    private final Grapher grapher = new Grapher();
    private final WorldClient worldClient;
    private final PlayerClient playerClient;
    private final Scheduler scheduler;

    private volatile World.Player player;
    private volatile Schedule runningTask;

    public Navigator(final WorldClient worldClient, final PlayerClient playerClient, final Scheduler scheduler) {
        this.worldClient = worldClient;
        this.playerClient = playerClient;
        this.scheduler = scheduler;
    }

    private void handlePlayerMoved(Event<PlayerMoved> event) {
        this.player = event.payload().player();
    }

    private void handleDestinationUpdated(final Event<DestinationUpdated> event) {
        if(player == null) {
            //Player hasn't spawned yet!
            return;
        }
        final var path = new ArrayDeque<>(grapher.findPathBetween(player.position(), event.payload().coordinates()));
        final RepeatingTask task = () -> {
            final var step = path.remove();
            worldClient.update(w -> w.move(step));
            return path.isEmpty();
        };
        runningTask = scheduler.scheduleRepeatingTask(task);

        //TODO Recalculate if path invalidated, e.g. world resized or wall built/destroyed
    }

    private void handleDestinationCleared(final Event<DestinationCleared> event) {
        runningTask.cancel();
        runningTask = null;
    }

    public void process(final List<Event<?>> batch) {
        grapher.process(batch);
        for(final var event : batch) {
            switch (event.name().value()) {
                case "PlayerMoved" -> handlePlayerMoved((Event<PlayerMoved>)event);
                case "DestinationUpdated" -> handleDestinationUpdated((Event<DestinationUpdated>)event);
                case "DestinationCleared" -> handleDestinationCleared((Event<DestinationCleared>)event);
            }
        }
    }

}
