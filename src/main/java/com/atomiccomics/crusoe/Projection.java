package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.world.World;

public final class Projection {

    private final World.Dimensions dimensions;
    private final int scaleFactor;

    public Projection(final World.Dimensions dimensions, final int scaleFactor) {
        this.dimensions = dimensions;
        this.scaleFactor = scaleFactor;
    }

    public double scaledWidth() {
        return dimensions.width() * scaleFactor;
    }

    public double scaledHeight() {
        return dimensions.height() * scaleFactor;
    }

    public double scaleFromWorldX(final double xCoord) {
        return xCoord * scaleFactor;
    }

    public double scaleFromWorldY(final double yCoord) {
        return (dimensions.height() - yCoord - 1) * scaleFactor;
    }

    public double scaleFromWorldSize(final double val) {
        return val * scaleFactor;
    }

    public double scaleToWorldX(final double xCoord) {
        return Math.floor(xCoord / scaleFactor);
    }

    public double scaleToWorldY(final double yCoord) {
        return Math.floor(dimensions.height() - (yCoord / scaleFactor));
    }

}
