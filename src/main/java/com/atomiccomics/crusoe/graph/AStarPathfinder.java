package com.atomiccomics.crusoe.graph;

import java.util.*;
import java.util.function.BiFunction;

public final class AStarPathfinder implements Pathfinder {

    private static final class WeightedNode<T> implements Comparable<WeightedNode<T>> {

        private final Graph.Node<T> node;
        private final long weight;

        private WeightedNode(final Graph.Node<T> node, final long weight) {
            this.node = node;
            this.weight = weight;
        }

        @Override
        public int compareTo(final WeightedNode o) {
            return Long.compare(weight, o.weight);
        }
    }

    @Override
    public <N, E> List<Graph.Edge<N, E>> findPathFrom(final Graph.Node<N> start,
                                         final Graph.Node<N> end,
                                         final Graph<N, E> graph,
                                         final BiFunction<Graph.Node<N>, Graph.Node<N>, Long> heuristic) throws ImpossiblePathException {

        final var frontier = new PriorityQueue<WeightedNode<N>>();
        frontier.add(new WeightedNode<N>(start, 0L));
        final var accumulatedCost = new HashMap<Graph.Node<N>, Long>();
        accumulatedCost.put(start, 0L);
        final var via = new HashMap<Graph.Node<N>, Graph.Edge<N, E>>();

        while(!frontier.isEmpty()) {
            final var current = frontier.remove();

            if(current.node == end) {
                break;
            }

            for(final var edge : graph.edges(current.node)) {
                final var neighbor = edge.to();
                final var newCost = accumulatedCost.get(current.node) + graph.cost(current.node, neighbor);
                if(!accumulatedCost.containsKey(neighbor) || newCost < accumulatedCost.get(neighbor)) {
                    accumulatedCost.put(neighbor, newCost);
                    final var weight = newCost + heuristic.apply(neighbor, end);
                    frontier.add(new WeightedNode<>(neighbor, weight));
                    via.put(neighbor, edge);
                }
            }
        }

        final var route = new LinkedList<Graph.Edge<N, E>>();
        Graph.Node<N> current = end;
        do {
            final var edge = via.get(current);
            if(edge == null) {
                // No edge pointing to current node
                throw new ImpossiblePathException();
            }
            route.addFirst(edge);
            current = edge.from();
        } while (current != start);

        if(!Objects.equals(route.get(0).from(), start)) {
            throw new ImpossiblePathException();
        }

        return List.copyOf(route);
    }
}
