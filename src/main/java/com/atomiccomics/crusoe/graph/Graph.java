package com.atomiccomics.crusoe.graph;

import java.util.*;
import java.util.stream.Collectors;

public final class Graph<N, E> {

    public static final class Node<T> {
        private final T value;

        private Node(T value) {
            this.value = value;
        }

        public T value() {
            return value;
        }
    }

    public static final class Edge<T, V> {
        private final Node<T> from;
        private final Node<T> to;
        private final V via;
        private final long cost;

        private Edge(final Node<T> from, final Node<T> to, final V via, final long cost) {
            this.from = from;
            this.to = to;
            this.via = via;
            this.cost = cost;
        }

        public Node<T> from() {
            return from;
        }

        public Node<T> to() {
            return to;
        }

        public V via() {
            return via;
        }
    }

    public static final class UndirectedGraphBuilder<N, E> {
        private final Map<Node<N>, Set<Edge<N, E>>> nodesToEdges = new HashMap<>();

        private UndirectedGraphBuilder() {

        }

        public Node<N> addNode(N value) {
            final var node = new Node<>(value);
            nodesToEdges.put(node, new HashSet<>());
            return node;
        }

        public void connect(Node<N> a, Node<N> b, E fromA, E fromB, long cost) {
            nodesToEdges.get(a).add(new Edge<>(a, b, fromA, cost));
            nodesToEdges.get(b).add(new Edge<>(b, a, fromB, cost));
        }

        public Graph<N, E> build() {
            return new Graph<>(Map.copyOf(nodesToEdges));
        }
    }

    public static final class DirectedGraphBuilder<N, E> {
        private final Map<Node<N>, Set<Edge<N, E>>> nodesToEdges = new HashMap<>();

        private DirectedGraphBuilder() {

        }

        public Node<N> addNode(N value) {
            final var node = new Node<>(value);
            nodesToEdges.put(node, new HashSet<>());
            return node;
        }

        public void connect(Node<N> a, Node<N> b, E via, long cost) {
            nodesToEdges.get(a).add(new Edge<>(a, b, via, cost));
        }

        public Graph<N, E> build() {
            return new Graph<>(Map.copyOf(nodesToEdges));
        }
    }

    public static <N, E> UndirectedGraphBuilder<N, E> undirectedGraphBuilder() {
        return new UndirectedGraphBuilder<>();
    }

    public static <N, E> DirectedGraphBuilder<N, E> directedGraphBuilder() {
        return new DirectedGraphBuilder<>();
    }

    private final Map<Node<N>, Set<Edge<N, E>>> nodes;

    private Graph(final Map<Node<N>, Set<Edge<N, E>>> nodes) {
        this.nodes = nodes;
    }

    public Optional<Node<N>> node(N value) {
        return nodes.keySet().stream()
                .filter(n -> Objects.equals(n.value(), value))
                .findFirst();
    }

    public Set<Edge<N, E>> edges(final Node<N> node) {
        assertContainsNode(node);

        return nodes.get(node);
    }

    public Set<Node<N>> from(final Node<N> node) {
        assertContainsNode(node);

        return nodes.get(node)
                .stream()
                .map(e -> e.to)
                .collect(Collectors.toSet());
    }

    public Set<Node<N>> to(final Node<N> node) {
        assertContainsNode(node);

        return nodes.entrySet()
                .stream()
                .filter(entry -> entry.getValue()
                    .stream()
                    .anyMatch(e -> Objects.equals(e.to, node)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public long cost(final Node<N> start, final Node<N> end) {
        assertContainsNode(start);
        assertContainsNode(end);

        return nodes.get(start)
                .stream()
                .filter(e -> e.to == end)
                .map(e -> e.cost)
                .findFirst()
                .orElseThrow();
    }

    private void assertContainsNode(final Node<N> node) {
        if(!nodes.containsKey(node)) {
            throw new IllegalStateException("This graph does not contain the provided node");
        }
    }

}
