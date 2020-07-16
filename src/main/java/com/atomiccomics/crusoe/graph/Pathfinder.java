package com.atomiccomics.crusoe.graph;

import java.util.List;
import java.util.function.BiFunction;

public interface Pathfinder {

    <N, E> List<Graph.Edge<N, E>> findPathFrom(Graph.Node<N> start,
                                               Graph.Node<N> end,
                                               Graph<N, E> graph,
                                               BiFunction<Graph.Node<N>, Graph.Node<N>, Long> heuristic)
            throws ImpossiblePathException;
}
