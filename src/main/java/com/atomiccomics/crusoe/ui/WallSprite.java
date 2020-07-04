package com.atomiccomics.crusoe.ui;

import com.atomiccomics.crusoe.world.World;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

final class WallSprite extends AbstractSprite {

    WallSprite(final World.Coordinates location) {
        super(location);
    }

    @Override
    protected void render(GraphicsContext graphics) {
        graphics.setFill(Color.rgb(255, 0, 0));
        graphics.fillRect(0, 0, 1, 1);
    }
}
