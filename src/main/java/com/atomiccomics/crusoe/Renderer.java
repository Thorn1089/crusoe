package com.atomiccomics.crusoe;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.Objects;

public class Renderer {

    private static final System.Logger LOG = System.getLogger(Renderer.class.getName());

    private static final int SCALE_FACTOR = 32;

    private final Canvas canvas;

    public Renderer(final Canvas canvas) {
        this.canvas = canvas;
    }

    public Runnable render(final World.WorldState state) {
        final var dimensions = state.dimensions();
        final var location = state.location();
        final var walls = state.walls();

        if(dimensions == null) {
            LOG.log(System.Logger.Level.TRACE, "World hasn't been sized yet");
            return () -> {};
        }

        return () -> {
            canvas.setWidth(SCALE_FACTOR * dimensions.width());
            canvas.setHeight(SCALE_FACTOR * dimensions.height());

            final var graphics = canvas.getGraphicsContext2D();
            graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            graphics.setStroke(Color.gray(0));
            graphics.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());

            final Color[] BACKGROUND_FILLS = new Color[] { Color.gray(0.8), Color.gray(0.6) };
            for(var i = 0; i < dimensions.width(); i++) {
                for(var j = 0; j < dimensions.height(); j++) {
                    final var currentCoordinates = new World.Coordinates(i, j);

                    if(Objects.equals(location, currentCoordinates)) {
                        graphics.setFill(Color.rgb(0, 255, 0));
                    } else if(walls.contains(currentCoordinates)) {
                        graphics.setFill(Color.rgb(255, 0, 0));
                    } else {
                        graphics.setFill(BACKGROUND_FILLS[(i + j) % 2]);
                    }
                    graphics.fillRect(projectX(dimensions, i), projectY(dimensions, j), SCALE_FACTOR, SCALE_FACTOR);
                }
            }
        };
    }

    private int projectX(final World.Dimensions dimensions, final int xCoord) {
        return xCoord * SCALE_FACTOR;
    }

    private int projectY(final World.Dimensions dimensions, final int yCoord) {
        return (dimensions.height() - yCoord - 1) * SCALE_FACTOR;
    }

}
