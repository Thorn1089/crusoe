package com.atomiccomics.crusoe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GameApplication extends Application {

    private static final System.Logger LOG = System.getLogger(GameApplication.class.getName());

    public static void main(final String... args) {
        LOG.log(System.Logger.Level.INFO, "Starting JavaFX application");
        Application.launch(args);
    }

    private ScheduledFuture<?> renderTask;
    private ScheduledFuture<?> ai;

    @Override
    public void start(final Stage stage) throws Exception {
        LOG.log(System.Logger.Level.DEBUG, "Displaying initial stage");

        final var canvas = new Canvas();
        canvas.setWidth(800);
        canvas.setHeight(600);

        final var scene = new Scene(new Pane(canvas));
        stage.setScene(scene);
        stage.show();

        final var random = new Random();

        final var state = new World.WorldState();
        final var mover = new Mover();

        final Consumer<List<Event<?>>> eventProcessor = batch -> {
            state.process(batch);
            mover.process(batch);
        };

        final var WIDTH = 32;
        final var HEIGHT = 32;

        eventProcessor.accept(new World(state).resize(new World.Dimensions(WIDTH, HEIGHT)));
        eventProcessor.accept(new World(state).spawnAt(new World.Coordinates(random.nextInt(WIDTH), random.nextInt(HEIGHT))));

        // Set up some random walls
        final var wallCount = random.nextInt(10) + 10;
        for(int i = 0; i < wallCount; i++) {
            eventProcessor.accept(new World(state).buildWallAt(new World.Coordinates(random.nextInt(WIDTH), random.nextInt(HEIGHT))));
        }

        final var executorService = Executors.newScheduledThreadPool(2);

        final var renderer = new Renderer(canvas);
        renderTask = executorService.scheduleAtFixedRate(() -> Platform.runLater(renderer.render(state)), 17, 17, TimeUnit.MILLISECONDS);

        ai = executorService.scheduleAtFixedRate(() -> {
            final var latestWorld = new World(state);
            final var possibilities = mover.legalMoves().toArray(new World.Direction[0]);
            final var direction = possibilities[random.nextInt(possibilities.length)];
            eventProcessor.accept(latestWorld.move(direction));
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        renderTask.cancel(true);
        ai.cancel(true);
    }
}
