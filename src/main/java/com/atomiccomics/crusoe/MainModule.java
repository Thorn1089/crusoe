package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.player.PlayerClient;
import com.atomiccomics.crusoe.player.ai.BuildWallActionFactory;
import com.atomiccomics.crusoe.player.ai.PickUpPickaxeActionFactory;
import com.atomiccomics.crusoe.player.ai.Planner;
import com.atomiccomics.crusoe.player.ai.PlayerMoveActionFactory;
import com.atomiccomics.crusoe.time.ExecutorScheduler;
import com.atomiccomics.crusoe.time.Scheduler;
import com.atomiccomics.crusoe.world.WorldClient;
import com.google.inject.*;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MainModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Engine.class).in(Scopes.SINGLETON);
        bind(GameController.class).in(Scopes.SINGLETON);
        bind(Scheduler.class).to(ExecutorScheduler.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    public ScheduledExecutorService schedulerPool() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Provides
    public PlayerClient playerClient(final Engine engine) {
        return engine::updatePlayer;
    }

    @Provides
    public WorldClient worldClient(final Engine engine) {
        return engine::updateWorld;
    }

    @Provides
    @Singleton
    public Planner planner(final Injector injector) {
        return new Planner(Arrays.asList(
             new BuildWallActionFactory(),
             new PickUpPickaxeActionFactory(),
             new PlayerMoveActionFactory()
        ), injector);
    }

}
