package com.atomiccomics.crusoe.player.ai;

import com.atomiccomics.crusoe.graph.ImpossiblePathException;
import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.world.Grapher;
import com.atomiccomics.crusoe.world.Mapper;

import java.util.Optional;

public class PickaxeIsReachablePrecondition implements Precondition {
    @Override
    public Optional<Effect> satisfiedBy() {
        return Optional.empty();
    }

    @Check
    public boolean canReachPickaxe(final Mapper mapper, final Grapher grapher) {
        final var player = mapper.playerLocation();
        final var pickaxe = mapper.itemLocation(Item.PICKAXE);

        return player.flatMap(p -> {
            return pickaxe.map(i -> {
                try {
                    grapher.findPathBetween(p, i);
                    return true;
                } catch (ImpossiblePathException e) {
                    return false;
                }
            });
        }).orElse(false);
    }
}
