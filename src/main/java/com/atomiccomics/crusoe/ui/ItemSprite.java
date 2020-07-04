package com.atomiccomics.crusoe.ui;

import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.world.World;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

final class ItemSprite extends AbstractSprite {

    private final Item item;

    ItemSprite(final World.Coordinates location, final Item item) {
        super(location);
        this.item = item;
    }

    @Override
    protected void render(GraphicsContext graphics) {
        graphics.setFill(Color.rgb(0, 0, 255));
        graphics.fillPolygon(new double[] { 0.0, 0.5, 1.0, 0.5 },
                new double[] { 0.5, 0.0, 0.5, 1.0 },
                4);
    }
}
