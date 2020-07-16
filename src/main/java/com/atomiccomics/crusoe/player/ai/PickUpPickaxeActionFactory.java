package com.atomiccomics.crusoe.player.ai;

public class PickUpPickaxeActionFactory extends TypeSafeActionFactory<PickaxePickedUpEffect> {
    public PickUpPickaxeActionFactory() {
        super(PickaxePickedUpEffect.class);
    }

    @Override
    protected Action safeCreate(PickaxePickedUpEffect effect) {
        return new PickUpPickaxeAction();
    }
}
