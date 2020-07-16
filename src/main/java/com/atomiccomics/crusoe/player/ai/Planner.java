package com.atomiccomics.crusoe.player.ai;

import com.atomiccomics.crusoe.graph.Graph;
import com.atomiccomics.crusoe.graph.AStarPathfinder;
import com.atomiccomics.crusoe.graph.ImpossiblePathException;
import com.atomiccomics.crusoe.graph.Pathfinder;
import com.google.inject.Injector;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Planner {

    private final Pathfinder pathfinder = new AStarPathfinder();
    private final List<ActionFactory> actionFactories;
    private final Injector injector;

    public Planner(final List<ActionFactory> actionFactories, final Injector injector) {
        this.actionFactories = actionFactories;
        this.injector = injector;
    }

    public List<Action> plan(final Goal goal) throws ImpossibleGoalException {
        final var builder = Graph.<Effect, Action>directedGraphBuilder();
        final var start = builder.addNode(null);

        /*
         * Ask goal to generate action set with effects that satisfy goal
         * Then ask each candidate action to generate action set with effects that satisfy preconditions
         * Once done, prune graph of any invalid starting actions (e.g. preconditions not met)
         * Use A* to find 'best' action chain in graph to satisfy end goal
         */
        final var effect = goal.satisfiedBy();
        final var end = builder.addNode(effect);
        buildGraph(builder, start, end);

        final var graph = builder.build();

        try {
            final var path = pathfinder.findPathFrom(start, end, graph, (a, b) -> 1L);
            return path.stream()
                    .map(Graph.Edge::via)
                    .collect(Collectors.toList());
        } catch (final ImpossiblePathException e) {
            throw new ImpossibleGoalException();
        }
    }

    private void buildGraph(final Graph.DirectedGraphBuilder<Effect, Action> builder, final Graph.Node<Effect> start, final Graph.Node<Effect> end) {
        final var effect = end.value();
        final var candidateActions = actionFactories.stream()
                .filter(f -> f.canProduce(effect))
                .map(f -> f.create(effect))
                .collect(Collectors.toSet());
        // Are the preconditions of these candidate actions met at present?
        for(final var candidate : candidateActions) {
            // Collect preconditions
            final var preconditions = candidate.preconditions();
            // Find any unsatisfied preconditions
            for (final var precondition : preconditions) {
                final var isSatisfied = Arrays.stream(precondition.getClass().getMethods())
                        .filter(m -> m.isAnnotationPresent(Check.class))
                        .map(m -> {
                            try {
                                final var params = Arrays.stream(m.getParameters())
                                        .map(p -> injector.getInstance(p.getType()))//TODO Handle qualifiers, etc.
                                        .toArray();
                                return (boolean) m.invoke(precondition, params);
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .reduce((a, b) -> a && b)
                        .orElse(true);
                if (!isSatisfied) {
                    final var childEffect = precondition.satisfiedBy();
                    childEffect.ifPresent(e -> {
                        final var step = builder.addNode(childEffect.get());
                        builder.connect(step, end, candidate, 1);

                        buildGraph(builder, start, step);
                    });
                } else {
                    builder.connect(start, end, candidate, 1);
                }
            }
        }
    }

}
