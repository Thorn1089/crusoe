package com.atomiccomics.crusoe.player.ai;

public class BuildWallActionFactory extends TypeSafeActionFactory<WallExistsEffect> {

    public BuildWallActionFactory() {
        super(WallExistsEffect.class);
    }

    @Override
    protected Action safeCreate(final WallExistsEffect effect) {
        return new BuildWallAction(effect.location());
    }
}
