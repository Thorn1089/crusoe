package com.atomiccomics.crusoe.ui;

import com.atomiccomics.crusoe.world.World;
import javafx.scene.canvas.GraphicsContext;

import java.util.function.Function;

public interface Sprite {

    void render(Function<World.Coordinates, GraphicsContext> graphicsSupplier);

}
