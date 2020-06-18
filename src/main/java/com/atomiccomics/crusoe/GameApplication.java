package com.atomiccomics.crusoe;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
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

    private final CompositeDisposable disposable = new CompositeDisposable();

    private ScheduledFuture<?> renderTask;

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

        final var keysToDirections = Map.of(
                KeyCode.W, World.Direction.NORTH,
                KeyCode.A, World.Direction.WEST,
                KeyCode.S, World.Direction.SOUTH,
                KeyCode.D, World.Direction.EAST
        );

        disposable.add(Observable.<KeyEvent>create(emitter -> {
                    final EventHandler<KeyEvent> listener = emitter::onNext;
                    emitter.setCancellable(() -> scene.removeEventHandler(KeyEvent.KEY_PRESSED, listener));
                    scene.addEventHandler(KeyEvent.KEY_PRESSED, listener);
                })
                .map(KeyEvent::getCode)
                .filter(keysToDirections::containsKey)
                .throttleFirst(250, TimeUnit.MILLISECONDS)
                .map(keysToDirections::get)
                .filter(d -> mover.legalMoves().contains(d))
                .subscribe(d -> eventProcessor.accept(new World(state).move(d))));
    }

    @Override
    public void stop() throws Exception {
        disposable.dispose();
        renderTask.cancel(true);
    }
}
