package com.atomiccomics.crusoe.player.ai;

import java.util.Set;

public interface Action {

    Set<Precondition> preconditions();

}
