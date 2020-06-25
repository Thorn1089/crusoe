package com.atomiccomics.crusoe.world;

import static org.hamcrest.CoreMatchers.is;

import com.atomiccomics.crusoe.Game;
import com.atomiccomics.crusoe.event.Event;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class PathfindingIT {

    @Test
    void pathBetweenPlayerAndGoalCanBeFollowed() {
        final var game = new Game();

        final var grapher = new Grapher();
        final var playerPositions = new LinkedList<World.Coordinates>();

        game.register(grapher::process);
        game.register(batch -> {
            for(final var event : batch) {
                if(event.name().value().equals("PlayerMoved")) {
                    final var moved = (Event<PlayerMoved>)event;
                    playerPositions.add(moved.payload().player().position());
                }
            }
        });

        game.updateWorld(w -> w.resize(new World.Dimensions(3, 3)));
        game.updateWorld(w -> w.spawnPlayerAt(new World.Coordinates(0, 0)));

        final var moves = grapher.findPathBetween(new World.Coordinates(0, 0), new World.Coordinates(2, 2));
        for(final var move : moves) {
            game.updateWorld(w -> w.move(move));
        }

        MatcherAssert.assertThat(playerPositions.getLast(), is(new World.Coordinates(2, 2)));
    }

}
