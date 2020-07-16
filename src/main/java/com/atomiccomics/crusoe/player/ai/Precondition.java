package com.atomiccomics.crusoe.player.ai;

import java.util.Optional;

public interface Precondition {

    Optional<Effect> satisfiedBy();

}
