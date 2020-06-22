package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.Player;
import com.atomiccomics.crusoe.world.World;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

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

        final List<Consumer<List<Event<?>>>> eventProcessors = new LinkedList<>();
        final Consumer<List<Event<?>>> eventProcessor = batch -> {
            eventProcessors.forEach(c -> c.accept(batch));
        };

        final var worldState = new World.WorldState();
        final var playerState = new Player.PlayerState();
        final var mover = new Mover();
        final var builder = new Builder();
        final var audioPlayer = new AudioPlayer();
        final var picker = new Picker(f -> eventProcessor.accept(f.apply(new Player(playerState))),
                f -> eventProcessor.accept(f.apply(new World(worldState))));

        eventProcessors.add(worldState::process);
        eventProcessors.add(playerState::process);
        eventProcessors.add(mover::process);
        eventProcessors.add(builder::process);
        eventProcessors.add(audioPlayer::process);
        eventProcessors.add(picker::process);

        final var WIDTH = 32;
        final var HEIGHT = 32;

        eventProcessor.accept(new World(worldState).resize(new World.Dimensions(WIDTH, HEIGHT)));

        // Set up some random walls
        final var wallCount = random.nextInt(10) + 10;
        IntStream.range(0, wallCount)
                .mapToObj(i -> new World.Coordinates(random.nextInt(WIDTH), random.nextInt(HEIGHT)))
                .distinct()
                .forEach(c -> eventProcessor.accept(new World(worldState).buildWallAt(c)));

        World.Coordinates playerStartsAt;
        do {
            playerStartsAt = new World.Coordinates(random.nextInt(WIDTH), random.nextInt(HEIGHT));
        } while(worldState.walls().contains(playerStartsAt));
        eventProcessor.accept(new World(worldState).spawnPlayerAt(playerStartsAt));

        World.Coordinates pickaxeStartsAt;
        do {
            pickaxeStartsAt = new World.Coordinates(random.nextInt(WIDTH), random.nextInt(HEIGHT));
        } while(worldState.walls().contains(pickaxeStartsAt) || playerStartsAt.equals(pickaxeStartsAt));
        eventProcessor.accept(new World(worldState).spawnItemAt(Item.PICKAXE, pickaxeStartsAt));

        final var renderer = new Renderer(canvas);

        disposable.add(Observable.interval(17, TimeUnit.MILLISECONDS)
            .subscribe(i -> Platform.runLater(renderer.render(worldState))));

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

        //TODO Implement diagonal movement - window events and join pairs of direction
        final var handlePlayerMovement = keysPressed
                .map(KeyEvent::getCode)
                .filter(keysToDirections::containsKey)
                .map(keysToDirections::get)
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .flatMap(direction -> {
                    if(worldState.player().orientation() == direction && mover.isLegalMove(direction)) {
                        return Observable.just(new World(worldState).move(direction));
                    } else {
                        return Observable.just(new World(worldState).turn(direction));
                    }
                });

        final var handlePlayerAction = keysPressed
                .map(KeyEvent::getCode)
                .filter(c -> c == KeyCode.E)
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .map(x -> worldState.player().lookingAt())
                .flatMap(spot -> {
                    if(builder.canBuildHere(spot)) {
                        return Observable.just(new World(worldState).buildWallAt(spot));
                    } else if(builder.canDestroyHere(spot)) {
                        return Observable.just(new World(worldState).destroyWallAt(spot));
                    }
                    return Observable.empty();
                });

        final var handlePlayerDrop = keysPressed
                .map(KeyEvent::getCode)
                .filter(c -> c == KeyCode.Q)
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .flatMap(x -> {
                    //TODO Don't check state object directly - use a read model
                    if(playerState.inventory().isEmpty()) {
                        return Observable.empty();
                    }
                    //TODO Decide which item to drop
                    return Observable.just(new Player(playerState).dropItem(Item.PICKAXE));
                });

        disposable.add(Observable.merge(Arrays.asList(
                handlePlayerMovement,
                handlePlayerAction,
                handlePlayerDrop)
        ).subscribe(eventProcessor::accept));

        disposable.add(keysPressed.map(KeyEvent::getCode).filter(c -> c == KeyCode.ESCAPE).subscribe(k -> Platform.exit()));
    }

    @Override
    public void stop() throws Exception {
        disposable.dispose();
    }
}
