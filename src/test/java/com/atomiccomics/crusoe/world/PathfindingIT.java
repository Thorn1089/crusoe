package com.atomiccomics.crusoe.world;

import static org.hamcrest.CoreMatchers.is;

import com.atomiccomics.crusoe.event.Event;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

public class PathfindingIT {

    @Test
    void pathBetweenPlayerAndGoalCanBeFollowed() {
        final var worldState = new World.WorldState();
        final var grapher = new Grapher();

        final Consumer<List<Event<?>>> eventHandler = batch -> {
            worldState.process(batch);
            grapher.process(batch);
        };

        eventHandler.accept(new World(worldState).resize(new World.Dimensions(3, 3)));
        eventHandler.accept(new World(worldState).spawnPlayerAt(new World.Coordinates(0, 0)));

        final var moves = grapher.findPathBetween(new World.Coordinates(0, 0), new World.Coordinates(2, 2));
        for(final var move : moves) {
            eventHandler.accept(new World(worldState).move(move));
        }

        MatcherAssert.assertThat(worldState.player().position(), is(new World.Coordinates(2, 2)));
    }

}
