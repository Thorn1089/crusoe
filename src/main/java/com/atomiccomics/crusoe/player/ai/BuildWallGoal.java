package com.atomiccomics.crusoe.player.ai;

import com.atomiccomics.crusoe.world.World;

public class BuildWallGoal implements Goal {

    private final World.Coordinates location;

    public BuildWallGoal(final World.Coordinates location) {
        this.location = location;
    }

    @Override
    public Effect satisfiedBy() {
        return new WallExistsEffect(location);
    }

}
