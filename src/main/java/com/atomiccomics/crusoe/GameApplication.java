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
        final var builder = new Builder();

        final Consumer<List<Event<?>>> eventProcessor = batch -> {
            state.process(batch);
            mover.process(batch);
            builder.process(batch);
        };

        final var WIDTH = 32;
        final var HEIGHT = 32;

        eventProcessor.accept(new World(state).resize(new World.Dimensions(WIDTH, HEIGHT)));

        // Set up some random walls
        final var wallCount = random.nextInt(10) + 10;
        for(int i = 0; i < wallCount; i++) {
            eventProcessor.accept(new World(state).buildWallAt(new World.Coordinates(random.nextInt(WIDTH), random.nextInt(HEIGHT))));
        }

        World.Coordinates playerStartsAt;
        do {
            playerStartsAt = new World.Coordinates(random.nextInt(WIDTH), random.nextInt(HEIGHT));
        } while(state.walls().contains(playerStartsAt));

        eventProcessor.accept(new World(state).spawnAt(playerStartsAt));

        final var renderer = new Renderer(canvas);

        disposable.add(Observable.interval(17, TimeUnit.MILLISECONDS)
            .subscribe(i -> Platform.runLater(renderer.render(state))));

        final var keysToDirections = Map.of(
                KeyCode.W, World.Direction.NORTH,
                KeyCode.A, World.Direction.WEST,
                KeyCode.S, World.Direction.SOUTH,
                KeyCode.D, World.Direction.EAST
        );

        final var keysPressed = Observable.<KeyEvent>create(emitter -> {
            final EventHandler<KeyEvent> listener = emitter::onNext;
            emitter.setCancellable(() -> scene.removeEventHandler(KeyEvent.KEY_PRESSED, listener));
            scene.addEventHandler(KeyEvent.KEY_PRESSED, listener);
        }).share();

        //TODO Implement diagonal movement - window events and join pairs of directions
        disposable.add(keysPressed
                .map(KeyEvent::getCode)
                .filter(keysToDirections::containsKey)
                .map(keysToDirections::get)
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .subscribe(direction -> {
                    if(state.player().orientation() == direction && mover.isLegalMove(direction)) {
                        eventProcessor.accept(new World(state).move(direction));
                    } else {
                        eventProcessor.accept(new World(state).turn(direction));
                    }
                }));

        disposable.add(keysPressed
                .map(KeyEvent::getCode)
                .filter(c -> c == KeyCode.E)
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .subscribe(k -> {
                    final var lookingAt = state.player().lookingAt();
                    if(builder.canBuildHere(lookingAt)) {
                        eventProcessor.accept(new World(state).buildWallAt(lookingAt));
                    }
                }));

        disposable.add(keysPressed.map(KeyEvent::getCode).filter(c -> c == KeyCode.ESCAPE).subscribe(k -> Platform.exit()));
    }

    @Override
    public void stop() throws Exception {
        disposable.dispose();
    }
}
