package com.atomiccomics.crusoe;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import com.atomiccomics.crusoe.world.World;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

@RunWith(JUnitQuickcheck.class)
public class WorldTest {

    public record DimensionPair(World.Dimensions larger, World.Dimensions smaller) {

    }

    public static final class DimensionPairGenerator extends Generator<DimensionPair> {

        public DimensionPairGenerator() {
            super(DimensionPair.class);
        }

        @Override
        public DimensionPair generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
            final int firstX = sourceOfRandomness.nextInt(1, 1_000_000);
            int secondX;
            do {
                secondX = sourceOfRandomness.nextInt(1, 1_000_000);
            } while(firstX == secondX);

            final int firstY = sourceOfRandomness.nextInt(1, 1_000_000);
            int secondY;
            do {
                secondY = sourceOfRandomness.nextInt(1, 1_000_000);
            } while(firstY == secondY);

            final var largerDimensions = new World.Dimensions(Math.max(firstX, secondX), Math.max(firstY, secondY));
            final var smallerDimensions = new World.Dimensions(Math.min(firstX, secondX), Math.min(firstY, secondY));

            return new DimensionPair(largerDimensions, smallerDimensions);
        }
    }

    @Property
    public void worldNeverContainsWallsOutsideBoundsAfterResizing(@From(DimensionPairGenerator.class) final DimensionPair pair) {
        final var state = new World.WorldState();
        state.process(new World(state).resize(pair.larger));

        final var diffX = pair.larger().width() - pair.smaller().width();
        final var diffY = pair.larger().height() - pair.smaller().height();
        final var wallCoords = new World.Coordinates(pair.smaller().width() + Math.floorDiv(diffX, 2),
                pair.smaller().height() + Math.floorDiv(diffY, 2));
        state.process(new World(state).buildWallAt(wallCoords));
        state.process(new World(state).resize(pair.smaller));

        MatcherAssert.assertThat(state.walls(), is(empty()));
    }

    @Property
    public void playerRelocatedInsideBoundsAfterResizing(@From(DimensionPairGenerator.class) final DimensionPair pair) {
        final var state = new World.WorldState();
        state.process(new World(state).resize(pair.larger));

        final var diffX = pair.larger().width() - pair.smaller().width();
        final var diffY = pair.larger().height() - pair.smaller().height();
        final var playerCoords = new World.Coordinates(pair.smaller().width() + Math.floorDiv(diffX, 2),
                pair.smaller().height() + Math.floorDiv(diffY, 2));
        state.process(new World(state).spawnPlayerAt(playerCoords));
        state.process(new World(state).resize(pair.smaller));

        Assertions.assertTrue(state.dimensions().contains(state.player().position()));
    }

    @Property
    public void playerFacingGivenDirectionAfterTurning(final World.Direction direction) {
        final var state = new World.WorldState();
        state.process(new World(state).resize(new World.Dimensions(64, 64)));
        state.process(new World(state).spawnPlayerAt(new World.Coordinates(32, 32)));

        state.process(new World(state).turn(direction));

        MatcherAssert.assertThat(state.player().orientation(), is(direction));
    }

}
