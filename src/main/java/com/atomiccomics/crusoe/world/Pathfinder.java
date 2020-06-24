package com.atomiccomics.crusoe.world;

import java.util.List;
import java.util.function.BiFunction;

public interface Pathfinder {

    List<Graph.Node> findPathFrom(Graph.Node start, Graph.Node end, Graph graph, BiFunction<Graph.Node, Graph.Node, Long> heuristic);

}
