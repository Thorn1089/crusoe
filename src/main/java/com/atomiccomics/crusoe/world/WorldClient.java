package com.atomiccomics.crusoe.world;

import com.atomiccomics.crusoe.event.Event;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface WorldClient {

    void update(Function<World, List<Event<?>>> updater);
}
