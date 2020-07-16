package com.atomiccomics.crusoe.player.ai;

import com.atomiccomics.crusoe.world.World;

import java.util.Collections;
import java.util.Set;

public class BuildWallAction implements Action {

    private final World.Coordinates location;

    public BuildWallAction(final World.Coordinates location) {
        this.location = location;
    }

    @Override
    public Set<Precondition> preconditions() {
        return Collections.singleton(new PlayerHasPickaxePrecondition());
    }
}
