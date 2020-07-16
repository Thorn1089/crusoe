package com.atomiccomics.crusoe.player;

import com.atomiccomics.crusoe.Handler;
import com.atomiccomics.crusoe.RegisteredComponent;
import com.atomiccomics.crusoe.player.ai.BuildWallGoal;
import com.atomiccomics.crusoe.player.ai.ImpossibleGoalException;
import com.atomiccomics.crusoe.player.ai.Planner;
import com.atomiccomics.crusoe.player.ai.PlayerMoveGoal;
import com.atomiccomics.crusoe.world.WallBlueprintPlaced;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@RegisteredComponent
public class Director {

    private static final System.Logger LOG = System.getLogger(Director.class.getName());

    private final Planner planner;

    @Inject
    public Director(final Planner planner) {
        this.planner = planner;
    }

    @Handler(DestinationUpdated.class)
    public void handleDestinationUpdated(final DestinationUpdated event) {
        try {
            final var steps = planner.plan(new PlayerMoveGoal(event.coordinates()));
            LOG.log(System.Logger.Level.DEBUG, "Created plan for goal: " + steps);
        } catch (final ImpossibleGoalException e) {
            LOG.log(System.Logger.Level.WARNING, "Unable to create plan for goal", e);
        }
    }

    @Handler(WallBlueprintPlaced.class)
    public void handleWallBlueprintPlaced(final WallBlueprintPlaced event) {
        try {
            final var steps = planner.plan(new BuildWallGoal(event.location()));
            LOG.log(System.Logger.Level.DEBUG, "Created plan for goal: " + steps);
        } catch (final ImpossibleGoalException e) {
            LOG.log(System.Logger.Level.WARNING, "Unable to create plan for goal", e);
        }
    }

}
