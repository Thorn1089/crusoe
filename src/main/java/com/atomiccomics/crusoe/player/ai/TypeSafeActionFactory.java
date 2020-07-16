package com.atomiccomics.crusoe.player.ai;

public abstract class TypeSafeActionFactory<T extends Effect> implements ActionFactory {

    private final Class<T> effectType;

    protected TypeSafeActionFactory(Class<T> effectType) {
        this.effectType = effectType;
    }

    @Override
    public final boolean canProduce(final Effect effect) {
        return effect.getClass().isAssignableFrom(effectType);
    }

    @Override
    public Action create(Effect effect) {
        return safeCreate(effectType.cast(effect));
    }

    protected abstract Action safeCreate(T effect);
}
