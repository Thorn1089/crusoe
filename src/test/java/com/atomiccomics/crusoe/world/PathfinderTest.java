package com.atomiccomics.crusoe.world;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.function.BiFunction;

public class PathfinderTest {

    @Test
    void pathfinderRoutesBetweenTwoPoints() {
        //Create a graph to search
        final var builder = Graph.newBuilder();

        final var width = 3;
        final var height = 3;
        final var matrix = new Graph.Node[width][height];
        final var xCoords = new HashMap<Graph.Node, Integer>();
        final var yCoords = new HashMap<Graph.Node, Integer>();
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                final var node = builder.addNode();

                matrix[i][j] = node;
                xCoords.put(node, i);
                yCoords.put(node, j);

                if(i > 0) {
                    builder.connect(matrix[i-1][j], node, 1);
                }
                if(j > 0) {
                    builder.connect(matrix[i][j-1], node, 1);
                }
            }
        }

        final var graph = builder.build();
        final var pathfinder = new AStarPathfinder();
        final BiFunction<Graph.Node, Graph.Node, Long> manhattanDistance = (a, b) -> {
            final var xDist = Math.abs(xCoords.get(a) - xCoords.get(b));
            final var yDist = Math.abs(yCoords.get(a) - yCoords.get(b));
            return (long)(xDist + yDist);
        };
        final var path = pathfinder.findPathFrom(matrix[0][0], matrix[2][2], graph, manhattanDistance);

        MatcherAssert.assertThat(path, hasSize(5));
    }

}
