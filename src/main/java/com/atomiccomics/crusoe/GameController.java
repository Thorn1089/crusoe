package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import com.atomiccomics.crusoe.item.Item;
import com.atomiccomics.crusoe.player.*;
import com.atomiccomics.crusoe.time.ExecutorScheduler;
import com.atomiccomics.crusoe.ui.AudioPlayer;
import com.atomiccomics.crusoe.ui.Drawer;
import com.atomiccomics.crusoe.ui.Projection;
import com.atomiccomics.crusoe.ui.Renderer;
import com.atomiccomics.crusoe.world.Grapher;
import com.atomiccomics.crusoe.world.World;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class GameController {

    private static final System.Logger LOG = System.getLogger(GameController.class.getName());

    private static final int WIDTH = 24;
    private static final int HEIGHT = 24;

    public class Goals {
        private final SimpleStringProperty description = new SimpleStringProperty();
        private final SimpleBooleanProperty hasGoal = new SimpleBooleanProperty();

        @Handler(DestinationUpdated.class)
        public void handleDestinationUpdated(final DestinationUpdated event) {
            Platform.runLater(() -> {
                hasGoal.set(true);
                description.set("Navigating to " + event.coordinates().x() + "," + event.coordinates().y());
            });
        }

        @Handler(DestinationCleared.class)
        public void handleDestinationCleared(final DestinationCleared event) {
            Platform.runLater(() -> {
                hasGoal.set(false);
                description.set("");
            });
        }
    }

    private final CompositeDisposable disposable = new CompositeDisposable();
    private final ScheduledExecutorService pool;

    @FXML private Canvas viewport;

    @FXML private Label goalDescription;

    @FXML private Button cancelGoal;

    public GameController(final ScheduledExecutorService pool) {
        this.pool = pool;
    }

    @FXML private void initialize() {
        LOG.log(System.Logger.Level.DEBUG, "GameController initialized");
    }

    public void start() {
        final var random = new Random();
        final var engine = new Engine();
        final var projection = new Projection(new World.Dimensions(WIDTH, HEIGHT), 32);

        final var scheduler = new ExecutorScheduler(pool);
        final var grapher = new Grapher();
        final var builder = new Builder();
        final var audioPlayer = new AudioPlayer();
        final var drawer = new Drawer();
        final var holder = new Holder();
        final var picker = new Picker(engine::updatePlayer, engine::updateWorld);
        final var navigator = new Navigator(grapher, engine::updateWorld, engine::updatePlayer, scheduler);
        final var runner = new Runner();

        final var goals = new Goals();
        goalDescription.textProperty().bind(goals.description);
        cancelGoal.disableProperty().bind(goals.hasGoal.not());

        engine.register(Component.wrap(grapher));
        engine.register(Component.wrap(builder));
        engine.register(audioPlayer::process);
        engine.register(Component.wrap(picker));
        engine.register(Component.wrap(drawer));
        engine.register(Component.wrap(holder));
        engine.register(Component.wrap(navigator));
        engine.register(Component.wrap(runner));
        engine.register(Component.wrap(scheduler));
        engine.register(Component.wrap(goals));

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

        final var renderer = new Renderer(viewport);

        disposable.add(Observable.interval(17, TimeUnit.MILLISECONDS)
                .subscribe(i -> Platform.runLater(renderer.render(drawer.snapshot(), projection))));

        final var keysPressed = Observable.<KeyEvent>create(emitter -> {
            final EventHandler<KeyEvent> listener = emitter::onNext;
            emitter.setCancellable(() -> viewport.removeEventHandler(KeyEvent.KEY_PRESSED, listener));
            viewport.addEventHandler(KeyEvent.KEY_PRESSED, listener);
        }).share();

        final var mouseClicked = Observable.<MouseEvent>create(emitter -> {
            final EventHandler<MouseEvent> listener = emitter::onNext;
            emitter.setCancellable(() -> viewport.removeEventHandler(MouseEvent.MOUSE_CLICKED, listener));
            viewport.addEventHandler(MouseEvent.MOUSE_CLICKED, listener);
        });

        final var buttonPressed = Observable.create(emitter -> {
            final EventHandler<ActionEvent> listener = emitter::onNext;
            emitter.setCancellable(() -> cancelGoal.removeEventHandler(ActionEvent.ACTION, listener));
            cancelGoal.addEventHandler(ActionEvent.ACTION, listener);
        });

        final Observable<Function<Player, List<Event<?>>>> updateFromPlayerNavigate = mouseClicked
                .filter(e -> e.getButton() == MouseButton.SECONDARY)
                .map(e -> new World.Coordinates((int)projection.scaleToWorldX(e.getX()), (int)projection.scaleToWorldY(e.getY())))
                .filter(navigator::isReachable)
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .map(dest -> p -> p.setDestination(dest));

        final Observable<Function<Player, List<Event<?>>>> updateFromPlayerCancel = buttonPressed
                .map(x -> Player::clearDestination);

        disposable.add(Observable.merge(
                updateFromPlayerNavigate,
                updateFromPlayerCancel)
                .filter(f -> runner.isGameRunning())
                .subscribe(engine::updatePlayer));

        disposable.add(keysPressed.map(KeyEvent::getCode).filter(c -> c == KeyCode.SPACE).subscribe(x -> {
            engine.updateGame(g -> runner.isGameRunning() ? g.pause() : g.resume());
        }));

        engine.updateGame(Game::resume);
    }

    public void stop() {
        disposable.dispose();
    }

}
