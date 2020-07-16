package com.atomiccomics.crusoe.player.ai;

import com.atomiccomics.crusoe.world.World;

public final class PlayerLocatedAtEffect implements Effect {

    private final World.Coordinates location;

    public PlayerLocatedAtEffect(final World.Coordinates location) {
        this.location = location;
    }

    public World.Coordinates location() {
        return location;
    }

}
