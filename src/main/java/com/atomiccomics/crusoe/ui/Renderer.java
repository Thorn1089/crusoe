package com.atomiccomics.crusoe.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class Renderer {

    private static final System.Logger LOG = System.getLogger(Renderer.class.getName());

    private final Canvas canvas;

    public Renderer(final Canvas canvas) {
        this.canvas = canvas;
    }

    public Runnable render(final Drawer.Frame frame, final Projection projection) {

        if(frame.dimensions() == null) {
            LOG.log(System.Logger.Level.TRACE, "World hasn't been sized yet");
            return () -> {};
        }

        return () -> {
            canvas.setWidth(projection.scaledWidth());
            canvas.setHeight(projection.scaledHeight());

            final var graphics = canvas.getGraphicsContext2D();

            graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            graphics.setStroke(Color.gray(0));
            graphics.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());

            final Color[] BACKGROUND_FILLS = new Color[] { Color.gray(0.8), Color.gray(0.6) };
            for(var i = 0; i < frame.dimensions().width(); i++) {
                for(var j = 0; j < frame.dimensions().height(); j++) {
                    graphics.setFill(BACKGROUND_FILLS[(i + j) % 2]);
                    graphics.fillRect(projection.scaleFromWorldX(i),
                            projection.scaleFromWorldY(j),
                            projection.scaleFromWorldSize(1),
                            projection.scaleFromWorldSize(1));
                }
            }

            for(final var sprite : frame.sprites()) {
                graphics.save();

                graphics.scale(projection.scaleFromWorldSize(1), projection.scaleFromWorldSize(1));
                sprite.render(coords -> {
                    graphics.translate(coords.x(), frame.dimensions().height() - coords.y() - 1);
                    return graphics;
                });

                graphics.restore();
            }

            if(!frame.isRunning()) {
                graphics.setStroke(Color.gray(0.2));
                graphics.setFill(Color.gray(0.8));
                graphics.strokeRect(projection.scaledWidth() / 5, projection.scaledHeight() / 5 * 2,
                        projection.scaledWidth() / 5 * 3, projection.scaledHeight() / 5);
                graphics.fillRect(projection.scaledWidth() / 5, projection.scaledHeight() / 5 * 2,
                        projection.scaledWidth() / 5 * 3, projection.scaledHeight() / 5);

                graphics.setFill(Color.gray(0.0));
                graphics.fillText("Paused", projection.scaledWidth() / 2 - 16, projection.scaledHeight() / 2);
            }
        };
    }

}
