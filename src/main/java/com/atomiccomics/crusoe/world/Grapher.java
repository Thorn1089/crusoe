package com.atomiccomics.crusoe.world;

import com.atomiccomics.crusoe.event.Event;

import java.util.*;
import java.util.function.BiFunction;

public final class Grapher {

    private volatile World.Dimensions dimensions;
    private final Set<World.Coordinates> obstacles = new HashSet<>();
    private volatile Map<World.Coordinates, Graph.Node> worldToGraph;
    private volatile  Map<Graph.Node, World.Coordinates> graphToWorld;

    private volatile boolean isDirty = false;
    private volatile Graph graph;

    private void handleWorldResized(final Event<WorldResized> event) {
        dimensions = event.payload().dimensions();
        isDirty = true;
    }

    private void handleWallBuilt(final Event<WallBuilt> event) {
        obstacles.add(event.payload().location());
        isDirty = true;
    }

    private void handleWallDestroyed(final Event<WallDestroyed> event) {
        obstacles.remove(event.payload().location());
        isDirty = true;
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            switch (event.name().value()) {
                case "WorldResized" -> handleWorldResized((Event<WorldResized>) event);
                case "WallBuilt" -> handleWallBuilt((Event<WallBuilt>)event);
                case "WallDestroyed" -> handleWallDestroyed((Event<WallDestroyed>)event);
            };
        }
    }

    private void rebuildGraph() {
        worldToGraph = new HashMap<>();
        graphToWorld = new HashMap<>();

        final var builder = Graph.newBuilder();
        final var matrix = new Graph.Node[dimensions.width()][dimensions.height()];
        for(int i = 0; i < dimensions.width(); i++) {
            for(int j = 0; j < dimensions.height(); j++) {
                final var coordinates = new World.Coordinates(i, j);
                if(obstacles.contains(coordinates)) {
                    continue;
                }

                final var node = builder.addNode();
                matrix[i][j] = node;
                worldToGraph.put(coordinates, node);
                graphToWorld.put(node, coordinates);

                if(i > 0) {
                    Optional.ofNullable(matrix[i-1][j]).ifPresent(n -> builder.connect(n, node, 1));
                }
                if(j > 0) {
                    Optional.ofNullable(matrix[i][j-1]).ifPresent(n -> builder.connect(n, node, 1));
                }
                if(i > 0 && j > 0) {
                    Optional.ofNullable(matrix[i-1][j-1]).ifPresent(n -> builder.connect(n, node, 1));
                }
                if(i > 0 && j < dimensions.height() - 1) {
                    Optional.ofNullable(matrix[i-1][j+1]).ifPresent(n -> builder.connect(n, node, 1));
                }
                //TODO Connect nodes diagonally
            }
        }

        graph = builder.build();
        isDirty = false;
    }

    public List<World.Direction> findPathBetween(final World.Coordinates start, final World.Coordinates end) {
        if(isDirty) {
            rebuildGraph();
        }

        final var pathfinder = new AStarPathfinder();
        final BiFunction<Graph.Node, Graph.Node, Long> manhattanDistance = (a, b) -> {
            final World.Coordinates first = graphToWorld.get(a);
            final World.Coordinates second = graphToWorld.get(b);

            return World.Coordinates.manhattanDistance(first, second);
        };

        final var path = new ArrayDeque<>(pathfinder.findPathFrom(worldToGraph.get(start), worldToGraph.get(end), graph, manhattanDistance));

        final var directions = new LinkedList<World.Direction>();
        Graph.Node prev = path.removeFirst();
        while(!path.isEmpty()) {
            final var curr = path.removeFirst();
            final var direction = graphToWorld.get(prev).to(graphToWorld.get(curr));
            directions.add(direction);
            prev = curr;
        }

        return directions;
    }

    public boolean isReachable(final World.Coordinates destination) {
        if(isDirty) {
            rebuildGraph();
        }

        return worldToGraph.containsKey(destination);
    }
}
