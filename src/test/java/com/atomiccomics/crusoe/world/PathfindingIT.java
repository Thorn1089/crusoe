package com.atomiccomics.crusoe.world;

import static org.hamcrest.CoreMatchers.is;

import com.atomiccomics.crusoe.Component;
import com.atomiccomics.crusoe.Engine;
import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.graph.ImpossiblePathException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class PathfindingIT {

    @Test
    void pathBetweenPlayerAndGoalCanBeFollowed() throws ImpossiblePathException {
        final var engine = new Engine();

        final var grapher = new Grapher();
        final var playerPositions = new LinkedList<World.Coordinates>();

        engine.register(Component.wrap(grapher));
        engine.register(batch -> {
            for(final var event : batch) {
                if(event.name().value().equals("PlayerMoved")) {
                    final var moved = (Event<PlayerMoved>)event;
                    playerPositions.add(moved.payload().player().position());
                }
            }
        });

        engine.updateWorld(w -> w.resize(new World.Dimensions(3, 3)));
        engine.updateWorld(w -> w.spawnPlayerAt(new World.Coordinates(0, 0)));

        final var moves = grapher.findPathBetween(new World.Coordinates(0, 0), new World.Coordinates(2, 2));
        for(final var move : moves) {
            engine.updateWorld(w -> w.move(move));
        }

        MatcherAssert.assertThat(playerPositions.getLast(), is(new World.Coordinates(2, 2)));
    }

}
