package com.atomiccomics.crusoe.player.ai;

public interface ActionFactory {

    boolean canProduce(Effect effect);

    Action create(Effect effect);

}
