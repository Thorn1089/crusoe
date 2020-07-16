package com.atomiccomics.crusoe.player.ai;

import com.atomiccomics.crusoe.MainModule;
import com.atomiccomics.crusoe.world.World;
import com.google.inject.Guice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class PlannerIT {

    @Test
    void plannerCannotBuildWallDueToNotBeingAbleToGetPickaxe() {
        final var injector = Guice.createInjector(new MainModule());

        final var planner = new Planner(Collections.emptyList(), injector);

        Assertions.assertThrows(ImpossibleGoalException.class, () -> planner.plan(new BuildWallGoal(new World.Coordinates(0, 0))));
    }

}
