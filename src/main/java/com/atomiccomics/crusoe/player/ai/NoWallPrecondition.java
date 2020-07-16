package com.atomiccomics.crusoe.player.ai;

import com.atomiccomics.crusoe.world.Grapher;
import com.atomiccomics.crusoe.world.World;

import java.util.Optional;

public final class NoWallPrecondition implements Precondition {

    private final World.Coordinates location;

    public NoWallPrecondition(final World.Coordinates location) {
        this.location = location;
    }

    @Override
    public Optional<Effect> satisfiedBy() {
        return Optional.empty();
    }

    @Check
    public boolean isLegalDestination(final Grapher grapher) {
        return grapher.isLegalDestination(location);
    }
}
