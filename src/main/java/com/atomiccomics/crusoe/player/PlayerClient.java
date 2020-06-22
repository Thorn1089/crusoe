package com.atomiccomics.crusoe.player;

import com.atomiccomics.crusoe.event.Event;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface PlayerClient {

    void update(Function<Player, List<Event<?>>> updater);

}
