package com.atomiccomics.crusoe.world;

import java.util.*;
import java.util.function.BiFunction;

public final class AStarPathfinder implements Pathfinder {

    private static final class WeightedNode implements Comparable<WeightedNode> {

        private final Graph.Node node;
        private final long weight;

        private WeightedNode(final Graph.Node node, final long weight) {
            this.node = node;
            this.weight = weight;
        }

        @Override
        public int compareTo(final WeightedNode o) {
            return Long.compare(weight, o.weight);
        }
    }

    @Override
    public List<Graph.Node> findPathFrom(final Graph.Node start,
                                         final Graph.Node end,
                                         final Graph graph,
                                         final BiFunction<Graph.Node, Graph.Node, Long> heuristic) {

        final var frontier = new PriorityQueue<WeightedNode>();
        frontier.add(new WeightedNode(start, 0L));
        final var accumulatedCost = new HashMap<Graph.Node, Long>();
        accumulatedCost.put(start, 0L);
        final var cameFrom = new HashMap<Graph.Node, Graph.Node>();

        while(!frontier.isEmpty()) {
            final var current = frontier.remove();

            if(current.node == end) {
                break;
            }

            for(final var neighbor : graph.neighbors(current.node)) {
                final var newCost = accumulatedCost.get(current.node) + graph.cost(current.node, neighbor);
                if(!accumulatedCost.containsKey(neighbor) || newCost < accumulatedCost.get(neighbor)) {
                    accumulatedCost.put(neighbor, newCost);
                    final var weight = newCost + heuristic.apply(neighbor, end);
                    frontier.add(new WeightedNode(neighbor, weight));
                    cameFrom.put(neighbor, current.node);
                }
            }
        }

        final var path = new LinkedList<Graph.Node>();
        Graph.Node current = end;
        do {
            path.addFirst(current);
            current = cameFrom.get(current);
        } while (current != null);

        return List.copyOf(path);
    }
}
