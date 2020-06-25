package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.world.World;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.Map;
import java.util.Objects;
import java.util.stream.DoubleStream;

public class Renderer {

    private record Triangle(double[] xCoords, double[] yCoords) {
        public Triangle {
            if(xCoords.length != 3) {
                throw new IllegalArgumentException("Incorrect number of x coordinates provided: " + xCoords.length);
            }
            if(yCoords.length != 3) {
                throw new IllegalArgumentException("Incorrect number of y coordinates provided: " + yCoords.length);
            }
        }
    }

    private static final System.Logger LOG = System.getLogger(Renderer.class.getName());

    private static final int SCALE_FACTOR = 32;

    private static final Map<World.Direction, Triangle> PLAYER_SHAPES = Map.of(
            World.Direction.NORTH, new Triangle(new double[] { 0.5, 1.0, 0.0 }, new double[] { 0.0, 1.0, 1.0 }),
            World.Direction.SOUTH, new Triangle(new double[] { 0.5, 1.0, 0.0 }, new double[] { 1.0, 0.0, 0.0 }),
            World.Direction.EAST, new Triangle(new double[] { 1.0, 0.0, 0.0 }, new double[] { 0.5, 1.0, 0.0 }),
            World.Direction.WEST, new Triangle(new double[] { 0.0, 1.0, 1.0 }, new double[] { 0.5, 1.0, 0.0 })
    );

    private final Canvas canvas;

    public Renderer(final Canvas canvas) {
        this.canvas = canvas;
    }

    public Runnable render(final Drawer.Frame frame) {

        if(frame.dimensions() == null) {
            LOG.log(System.Logger.Level.TRACE, "World hasn't been sized yet");
            return () -> {};
        }

        return () -> {
            canvas.setWidth(SCALE_FACTOR * frame.dimensions().width());
            canvas.setHeight(SCALE_FACTOR * frame.dimensions().height());

            final var graphics = canvas.getGraphicsContext2D();
            graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            graphics.setStroke(Color.gray(0));
            graphics.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());

            final Color[] BACKGROUND_FILLS = new Color[] { Color.gray(0.8), Color.gray(0.6) };
            for(var i = 0; i < frame.dimensions().width(); i++) {
                for(var j = 0; j < frame.dimensions().height(); j++) {
                    final var currentCoordinates = new World.Coordinates(i, j);

                    if(frame.walls().contains(currentCoordinates)) {
                        graphics.setFill(Color.rgb(255, 0, 0));
                    } else {
                        graphics.setFill(BACKGROUND_FILLS[(i + j) % 2]);
                    }
                    graphics.fillRect(projectX(frame.dimensions(), i), projectY(frame.dimensions(), j), SCALE_FACTOR, SCALE_FACTOR);

                    if(Objects.equals(frame.player().position(), currentCoordinates)) {
                        graphics.setFill(Color.rgb(0, 255, 0));
                        final var triangle = PLAYER_SHAPES.get(frame.player().orientation());
                        final var xOrigin = i;
                        final var yOrigin = j;
                        graphics.fillPolygon(
                                DoubleStream.of(triangle.xCoords()).map(d -> projectX(frame.dimensions(), xOrigin) + d * SCALE_FACTOR).toArray(),
                                DoubleStream.of(triangle.yCoords()).map(d -> projectY(frame.dimensions(), yOrigin) + d * SCALE_FACTOR).toArray(),
                                3);
                    } else if(frame.items().containsKey(currentCoordinates)) {
                        //TODO Decide what polygon to draw based on item type
                        graphics.setFill(Color.rgb(0, 0, 255));
                        final var xOrigin = i;
                        final var yOrigin = j;
                        graphics.fillPolygon(
                                DoubleStream.of(0.0, 0.5, 1.0, 0.5).map(d -> projectX(frame.dimensions(), xOrigin) + d * SCALE_FACTOR).toArray(),
                                DoubleStream.of(0.5, 0.0, 0.5, 1.0).map(d -> projectY(frame.dimensions(), yOrigin) + d * SCALE_FACTOR).toArray(),
                                4);
                    }
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
