package com.atomiccomics.crusoe.world;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.atomiccomics.crusoe.graph.AStarPathfinder;
import com.atomiccomics.crusoe.graph.Graph;
import com.atomiccomics.crusoe.graph.ImpossiblePathException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

public class PathfinderTest {

    @Test
    void pathfinderRoutesBetweenTwoPoints() throws ImpossiblePathException {
        //Create a graph to search
        final var builder = Graph.undirectedGraphBuilder();

        final var width = 3;
        final var height = 3;
        final var matrix = new ArrayList<ArrayList<Graph.Node<Object>>>();
        final var xCoords = new HashMap<Graph.Node<Object>, Integer>();
        final var yCoords = new HashMap<Graph.Node<Object>, Integer>();
        for(int i = 0; i < width; i++) {
            final var innerList = new ArrayList<Graph.Node<Object>>();
            matrix.add(innerList);
            for(int j = 0; j < height; j++) {
                final var node = builder.addNode(null);

                innerList.add(node);
                xCoords.put(node, i);
                yCoords.put(node, j);

                if(i > 0) {
                    builder.connect(matrix.get(i-1).get(j), node, null, null, 1);
                }
                if(j > 0) {
                    builder.connect(matrix.get(i).get(j-1), node, null, null, 1);
                }
            }
        }

        final var graph = builder.build();
        final var pathfinder = new AStarPathfinder();
        final BiFunction<Graph.Node<Object>, Graph.Node<Object>, Long> manhattanDistance = (a, b) -> {
            final var xDist = Math.abs(xCoords.get(a) - xCoords.get(b));
            final var yDist = Math.abs(yCoords.get(a) - yCoords.get(b));
            return (long)(xDist + yDist);
        };
        final var path = pathfinder.findPathFrom(matrix.get(0).get(0), matrix.get(2).get(2), graph, manhattanDistance);

        MatcherAssert.assertThat(path, hasSize(4));
    }

}
