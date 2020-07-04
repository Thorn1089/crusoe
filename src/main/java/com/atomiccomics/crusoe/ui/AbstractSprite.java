package com.atomiccomics.crusoe.ui;

import com.atomiccomics.crusoe.world.World;
import javafx.scene.canvas.GraphicsContext;

import java.util.function.Function;

abstract class AbstractSprite implements Sprite {

    protected final World.Coordinates location;

    protected AbstractSprite(final World.Coordinates location) {
        this.location = location;
    }

    @Override
    public final void render(Function<World.Coordinates, GraphicsContext> graphicsSupplier) {
        final var graphics = graphicsSupplier.apply(location);
        render(graphics);
    }

    protected abstract void render(GraphicsContext graphics);
}
