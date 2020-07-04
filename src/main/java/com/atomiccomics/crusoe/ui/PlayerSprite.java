package com.atomiccomics.crusoe.ui;

import com.atomiccomics.crusoe.world.World;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Map;

final class PlayerSprite extends AbstractSprite {

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

    private static final Map<World.Direction, Triangle> PLAYER_SHAPES = Map.of(
            World.Direction.NORTH, new Triangle(new double[] { 0.5, 1.0, 0.0 }, new double[] { 0.0, 1.0, 1.0 }),
            World.Direction.SOUTH, new Triangle(new double[] { 0.5, 1.0, 0.0 }, new double[] { 1.0, 0.0, 0.0 }),
            World.Direction.EAST, new Triangle(new double[] { 1.0, 0.0, 0.0 }, new double[] { 0.5, 1.0, 0.0 }),
            World.Direction.WEST, new Triangle(new double[] { 0.0, 1.0, 1.0 }, new double[] { 0.5, 1.0, 0.0 }),
            World.Direction.NORTHEAST, new Triangle(new double[]{ 0.75, 1.0, 0.0 }, new double[]{ 1.0, 0.0, 0.25 }),
            World.Direction.NORTHWEST, new Triangle(new double[]{ 0.0, 0.25, 1.0 }, new double[]{ 0.0, 1.0, 0.25 }),
            World.Direction.SOUTHEAST, new Triangle(new double[]{ 0.75, 1.0, 0.0 }, new double[]{ 0.0, 1.0, 0.75 }),
            World.Direction.SOUTHWEST, new Triangle(new double[]{ 0.0, 0.25, 1.0 }, new double[]{ 1.0, 0.0, 0.75 })
    );

    private final World.Direction orientation;
    private final boolean isSelected;

    PlayerSprite(final World.Coordinates location,
                 final World.Direction orientation,
                 final boolean isSelected) {
        super(location);
        this.orientation = orientation;
        this.isSelected = isSelected;
    }

    @Override
    protected void render(GraphicsContext graphics) {
        graphics.setFill(Color.rgb(0, 255, 0));
        final var triangle = PLAYER_SHAPES.get(orientation);
        graphics.fillPolygon(triangle.xCoords(), triangle.yCoords(), 3);

        if(isSelected) {
            graphics.setStroke(Color.rgb(127, 0, 127));
            graphics.setLineWidth(0.1);
            graphics.strokeRect(0, 0, 1, 1);
        }
    }
}
