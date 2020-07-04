package com.atomiccomics.crusoe.ui;

import com.atomiccomics.crusoe.world.World;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

final class DestinationSprite extends AbstractSprite {

    DestinationSprite(final World.Coordinates coordinates) {
        super(coordinates);
    }

    @Override
    protected void render(GraphicsContext graphics) {
        graphics.setFill(Color.rgb(0, 64, 128));
        graphics.fillRect(0, 0, 1, 1);
    }
}
