package com.atomiccomics.crusoe.player.ai;

import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.Holder;

import java.util.Optional;

public class PlayerHasPickaxePrecondition implements Precondition {
    @Override
    public Optional<Effect> satisfiedBy() {
        return Optional.of(new PickaxePickedUpEffect());
    }

    @Check
    public boolean hasPickaxe(final Holder holder) {
        return holder.has(Item.PICKAXE);
    }
}
