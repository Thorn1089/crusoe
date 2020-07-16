package com.atomiccomics.crusoe.player.ai;

import java.util.Collections;
import java.util.Set;

public final class PickUpPickaxeAction implements Action {
    @Override
    public Set<Precondition> preconditions() {
        return Collections.singleton(new PickaxeIsReachablePrecondition());
    }
}
