package com.atomiccomics.crusoe.player.ai;

import com.atomiccomics.crusoe.world.World;

import java.util.Collections;
import java.util.Set;

public final class PlayerMoveAction implements Action {

    private final World.Coordinates location;

    public PlayerMoveAction(final World.Coordinates location) {
        this.location = location;
    }

    @Override
    public Set<Precondition> preconditions() {
        return Collections.singleton(new NoWallPrecondition(location));
    }
}
