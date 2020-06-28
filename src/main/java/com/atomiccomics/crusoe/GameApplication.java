package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.Navigator;
import com.atomiccomics.crusoe.player.Player;
import com.atomiccomics.crusoe.time.ExecutorScheduler;
import com.atomiccomics.crusoe.world.Grapher;
import com.atomiccomics.crusoe.world.World;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameApplication extends Application {

    private static final System.Logger LOG = System.getLogger(GameApplication.class.getName());

    public record ScreenSize(int width, int height) {

    }

    public static void main(final String... args) {
        LOG.log(System.Logger.Level.INFO, "Starting JavaFX application");
        Application.launch(args);
    }

    private final CompositeDisposable disposable = new CompositeDisposable();

    private final ExecutorScheduler scheduler = new ExecutorScheduler(Executors.newSingleThreadScheduledExecutor());

    @Override
    public void start(final Stage stage) throws Exception {
        LOG.log(System.Logger.Level.DEBUG, "Displaying initial stage");

        final var WIDTH = 24;
        final var HEIGHT = 24;
        final var projection = new Projection(new World.Dimensions(WIDTH, HEIGHT), 32);

        final var canvas = new Canvas();

        final var scene = new Scene(new Pane(canvas), projection.scaleFromWorldSize(WIDTH), projection.scaleFromWorldSize(HEIGHT));
        stage.setScene(scene);

        final var engine = new Engine();

        final var random = new Random();

        final Grapher grapher = new Grapher();
        final var builder = new Builder();
        final var audioPlayer = new AudioPlayer();
        final var drawer = new Drawer();
        final var holder = new Holder();
        final var picker = new Picker(engine::updatePlayer, engine::updateWorld);
        final var navigator = new Navigator(grapher, engine::updateWorld, engine::updatePlayer, scheduler);

        engine.register(Component.wrap(grapher));
        engine.register(Component.wrap(builder));
        engine.register(audioPlayer::process);
        engine.register(Component.wrap(picker));
        engine.register(Component.wrap(drawer));
        engine.register(Component.wrap(holder));
        engine.register(Component.wrap(navigator));

        engine.updateWorld(w -> w.resize(new World.Dimensions(WIDTH, HEIGHT)));

        // Set up some random walls
        final var wallCount = random.nextInt(10) + 10;
        final var walls = IntStream.range(0, wallCount)
                .mapToObj(i -> new World.Coordinates(random.nextInt(WIDTH), random.nextInt(HEIGHT)))
                .collect(Collectors.toSet());
        walls.forEach(c -> engine.updateWorld(w -> w.buildWallAt(c)));

        World.Coordinates candidateStartingLocation;
        do {
            candidateStartingLocation = new World.Coordinates(random.nextInt(WIDTH), random.nextInt(HEIGHT));
        } while(walls.contains(candidateStartingLocation));
        final var playerStartsAt = candidateStartingLocation;
        engine.updateWorld(w -> w.spawnPlayerAt(playerStartsAt));

        World.Coordinates candidateItemLocation;
        do {
            candidateItemLocation = new World.Coordinates(random.nextInt(WIDTH), random.nextInt(HEIGHT));
        } while(walls.contains(candidateItemLocation) || playerStartsAt.equals(candidateItemLocation));
        final var pickaxeStartsAt = candidateItemLocation;
        engine.updateWorld(w -> w.spawnItemAt(Item.PICKAXE, pickaxeStartsAt));

        final var renderer = new Renderer(canvas);

        disposable.add(Observable.interval(17, TimeUnit.MILLISECONDS)
            .subscribe(i -> Platform.runLater(renderer.render(drawer.snapshot(), projection))));

        final var keysPressed = Observable.<KeyEvent>create(emitter -> {
            final EventHandler<KeyEvent> listener = emitter::onNext;
            emitter.setCancellable(() -> scene.removeEventHandler(KeyEvent.KEY_PRESSED, listener));
            scene.addEventHandler(KeyEvent.KEY_PRESSED, listener);
        }).share();

        final var mouseClicked = Observable.<MouseEvent>create(emitter -> {
            final EventHandler<MouseEvent> listener = emitter::onNext;
            emitter.setCancellable(() -> canvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, listener));
            canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, listener);
        });

        disposable.add(mouseClicked
                .filter(e -> e.getButton() == MouseButton.PRIMARY)
                .map(e -> new World.Coordinates((int)projection.scaleToWorldX(e.getX()), (int)projection.scaleToWorldY(e.getY())))
                .filter(navigator::isReachable)
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .subscribe(dest -> {
                    engine.updatePlayer(p -> p.setDestination(dest));
                }));

        disposable.add(mouseClicked
                .filter(e -> e.getButton() == MouseButton.SECONDARY)
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .subscribe(x -> engine.updatePlayer(Player::clearDestination)));

        final Observable<Function<World, List<Event<?>>>> updateFromPlayerAction = keysPressed
                .map(KeyEvent::getCode)
                .filter(c -> c == KeyCode.E)
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .flatMap(x -> {
                    if(builder.canBuildWherePlayerLooking()) {
                        return Observable.just(w -> w.buildWallAt(builder.playerTarget()));
                    } else if(builder.canDestroyWherePlayerLooking()) {
                        return Observable.just(w -> w.destroyWallAt(builder.playerTarget()));
                    }
                    return Observable.empty();
                });

        final Observable<Function<Player, List<Event<?>>>> updateFromPlayerDrop = keysPressed
                .map(KeyEvent::getCode)
                .filter(c -> c == KeyCode.Q)
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .flatMap(x -> {
                    if(holder.hasItems()) {
                        //TODO Decide which item to drop
                        return Observable.just(p -> p.dropItem(Item.PICKAXE));
                    }
                    return Observable.empty();
                });

        disposable.add(updateFromPlayerAction
                .subscribe(engine::updateWorld));
        disposable.add(updateFromPlayerDrop.subscribe(engine::updatePlayer));

        disposable.add(keysPressed.map(KeyEvent::getCode).filter(c -> c == KeyCode.ESCAPE).subscribe(k -> Platform.exit()));

        disposable.add(keysPressed.map(KeyEvent::getCode).filter(c -> c == KeyCode.SPACE).subscribe(x -> {
            if(scheduler.isRunning()) {
                scheduler.stop();
            } else {
                scheduler.start();
            }
        }));

        final var screenWidth = Observable.<Number>create(emitter -> {
            final ChangeListener<Number> listener = (obs, oldVal, newVal) -> emitter.onNext(newVal);
            emitter.setCancellable(() -> stage.widthProperty().removeListener(listener));
            stage.widthProperty().addListener(listener);
        });
        final var screenHeight = Observable.<Number>create(emitter -> {
            final ChangeListener<Number> listener = (obs, oldVal, newVal) -> emitter.onNext(newVal);
            emitter.setCancellable(() -> stage.heightProperty().removeListener(listener));
            stage.heightProperty().addListener(listener);
        });

        disposable.add(Observable.combineLatest(screenWidth, screenHeight, (w, h) -> new ScreenSize(w.intValue(), h.intValue()))
            .debounce(250, TimeUnit.MILLISECONDS)
            .subscribe(s -> {
                LOG.log(System.Logger.Level.DEBUG, "Screen resized: " + s);
            }));

        scheduler.start();
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        scheduler.stop();
        disposable.dispose();
    }
}
