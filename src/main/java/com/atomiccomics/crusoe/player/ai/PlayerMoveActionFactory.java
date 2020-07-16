package com.atomiccomics.crusoe.player.ai;

public final class PlayerMoveActionFactory extends TypeSafeActionFactory<PlayerLocatedAtEffect> {

    public PlayerMoveActionFactory() {
        super(PlayerLocatedAtEffect.class);
    }

    @Override
    protected Action safeCreate(final PlayerLocatedAtEffect effect) {
        return new PlayerMoveAction(effect.location());
    }
}
