package com.atomiccomics.crusoe.world;

import com.atomiccomics.crusoe.Handler;
import com.atomiccomics.crusoe.RegisteredComponent;
import com.atomiccomics.crusoe.graph.AStarPathfinder;
import com.atomiccomics.crusoe.graph.Graph;
import com.atomiccomics.crusoe.graph.ImpossiblePathException;
import com.google.inject.Singleton;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Singleton
@RegisteredComponent
public final class Grapher {

    private volatile World.Dimensions dimensions;
    private final Set<World.Coordinates> obstacles = new HashSet<>();

    private volatile boolean isDirty = false;
    private volatile Graph<World.Coordinates, World.Direction> graph;

    @Handler(WorldResized.class)
    public void handleWorldResized(final WorldResized event) {
        dimensions = event.dimensions();
        isDirty = true;
    }

    @Handler(WallBuilt.class)
    public void handleWallBuilt(final WallBuilt event) {
        obstacles.add(event.location());
        isDirty = true;
    }

    @Handler(WallDestroyed.class)
    public void handleWallDestroyed(final WallDestroyed event) {
        obstacles.remove(event.location());
        isDirty = true;
    }

    private void rebuildGraph() {
        final Graph.UndirectedGraphBuilder<World.Coordinates, World.Direction> builder = Graph.undirectedGraphBuilder();
        final var matrix = new ArrayList<ArrayList<Graph.Node<World.Coordinates>>>(dimensions.width());
        for(int i = 0; i < dimensions.width(); i++) {
            final var innerList = new ArrayList<Graph.Node<World.Coordinates>>(dimensions.height());
            matrix.add(i, innerList);
            for(int j = 0; j < dimensions.height(); j++) {
                final var coordinates = new World.Coordinates(i, j);
                if(obstacles.contains(coordinates)) {
                    innerList.add(j, null);
                    continue;
                }

                final var node = builder.addNode(coordinates);
                innerList.add(j, node);

                if(i > 0) {
                    Optional.ofNullable(matrix.get(i-1).get(j)).ifPresent(n -> builder.connect(n, node, World.Direction.EAST, World.Direction.WEST, 1));
                }
                if(j > 0) {
                    Optional.ofNullable(matrix.get(i).get(j-1)).ifPresent(n -> builder.connect(n, node, World.Direction.NORTH, World.Direction.SOUTH, 1));
                }
                if(i > 0 && j > 0) {
                    Optional.ofNullable(matrix.get(i-1).get(j-1)).ifPresent(n -> builder.connect(n, node, World.Direction.NORTHEAST, World.Direction.SOUTHWEST, 1));
                }
                if(i > 0 && j < dimensions.height() - 1) {
                    Optional.ofNullable(matrix.get(i-1).get(j+1)).ifPresent(n -> builder.connect(n, node, World.Direction.SOUTHEAST, World.Direction.NORTHWEST, 1));
                }
            }
        }

        graph = builder.build();
        isDirty = false;
    }

    public List<World.Direction> findPathBetween(final World.Coordinates start, final World.Coordinates end) throws ImpossiblePathException {
        if(isDirty) {
            rebuildGraph();
        }

        final var startNode = graph.node(start);
        final var endNode = graph.node(end);

        if(startNode.isEmpty() || endNode.isEmpty()) {
            throw new IllegalArgumentException("Cannot route between nodes when one of them is not present in the graph");
        }

        final var pathfinder = new AStarPathfinder();
        final BiFunction<Graph.Node<World.Coordinates>, Graph.Node<World.Coordinates>, Long> manhattanDistance = (a, b) -> World.Coordinates.manhattanDistance(a.value(), b.value());

        final var path = pathfinder.findPathFrom(startNode.get(), endNode.get(), graph, manhattanDistance);

        return path.stream()
                .map(Graph.Edge::via)
                .collect(Collectors.toList());
    }

    public boolean isLegalDestination(final World.Coordinates destination) {
        if(isDirty) {
            rebuildGraph();
        }

        return graph.node(destination).isPresent();
    }
}
