package com.atomiccomics.crusoe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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

    private ScheduledFuture<?> renderer;
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

        eventProcessor.accept(new World(state).resize(new World.Width(32), new World.Height(32)));
        eventProcessor.accept(new World(state).spawnAt(new World.Coordinates(random.nextInt(32), random.nextInt(32))));

        final var executorService = Executors.newScheduledThreadPool(2);
        renderer = executorService.scheduleAtFixedRate(() -> {
            final int SCALE_FACTOR = 32;

            final var width = state.width();
            final var height = state.height();
            final var location = state.location();

            if(width == null || height == null) {
                LOG.log(System.Logger.Level.TRACE, "World hasn't been sized yet");
                return;
            }

            Platform.runLater(() -> {
                canvas.setWidth(SCALE_FACTOR * width.size());
                canvas.setHeight(SCALE_FACTOR * height.size());

                final var graphics = canvas.getGraphicsContext2D();
                graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                graphics.setStroke(Color.gray(0));
                graphics.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());

                final Color[] BACKGROUND_FILLS = new Color[] { Color.gray(0.8), Color.gray(0.6) };
                for(var i = 0; i < width.size(); i++) {
                    for(var j = 0; j < height.size(); j++) {
                        if(location != null && location.x() == i && location.y() == j) {
                            graphics.setFill(Color.rgb(0, 255, 0));
                        } else {
                            graphics.setFill(BACKGROUND_FILLS[(i + j) % 2]);
                        }
                        graphics.fillRect(i * SCALE_FACTOR, j * SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
                    }
                }
            });
        }, 17, 17, TimeUnit.MILLISECONDS);


        ai = executorService.scheduleAtFixedRate(() -> {
            final var latestWorld = new World(state);
            final var possibilities = mover.legalMoves().toArray(new World.Direction[0]);
            final var direction = possibilities[random.nextInt(possibilities.length)];
            eventProcessor.accept(latestWorld.move(direction));
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        renderer.cancel(true);
        ai.cancel(true);
    }
}
