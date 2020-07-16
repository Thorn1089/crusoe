package com.atomiccomics.crusoe.player.ai;

import com.atomiccomics.crusoe.world.World;

public class PlayerMoveGoal implements Goal {

    private final World.Coordinates location;

    public PlayerMoveGoal(final World.Coordinates location) {
        this.location = location;
    }

    @Override
    public Effect satisfiedBy() {
        return new PlayerLocatedAtEffect(location);
    }
}
