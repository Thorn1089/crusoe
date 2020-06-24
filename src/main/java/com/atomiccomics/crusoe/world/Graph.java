package com.atomiccomics.crusoe.world;

import java.util.*;
import java.util.stream.Collectors;

public final class Graph {

    public static final class Node {
        private Node() {

        }
    }

    private static final class Edge {
        private final Node to;
        private final long cost;

        private Edge(final Node to, final long cost) {
            this.to = to;
            this.cost = cost;
        }
    }

    public static final class Builder {
        private final Map<Node, Set<Edge>> nodes = new HashMap<>();

        private Builder() {

        }

        public Node addNode() {
            final var node = new Node();
            nodes.put(node, new HashSet<>());
            return node;
        }

        public void connect(Node a, Node b, long cost) {
            nodes.get(a).add(new Edge(b, cost));
            nodes.get(b).add(new Edge(a, cost));
        }

        public Graph build() {
            return new Graph(Map.copyOf(nodes));
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private final Map<Node, Set<Edge>> nodes;

    private Graph(final Map<Node, Set<Edge>> nodes) {
        this.nodes = nodes;
    }

    public Set<Node> neighbors(final Node node) {
        assertContainsNode(node);

        return nodes.get(node)
                .stream()
                .map(e -> e.to)
                .collect(Collectors.toSet());
    }

    public long cost(final Node start, final Node end) {
        assertContainsNode(start);
        assertContainsNode(end);

        return nodes.get(start)
                .stream()
                .filter(e -> e.to == end)
                .map(e -> e.cost)
                .findFirst()
                .orElseThrow();
    }

    private void assertContainsNode(final Node node) {
        if(!nodes.containsKey(node)) {
            throw new IllegalStateException("This graph does not contain the provided node");
        }
    }

}
