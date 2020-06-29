package com.atomiccomics.crusoe;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameApplication extends Application {

    private static final System.Logger LOG = System.getLogger(GameApplication.class.getName());

    public record ScreenSize(int width, int height) {

    }

    public static void main(final String... args) {
        LOG.log(System.Logger.Level.INFO, "Starting JavaFX application");
        Application.launch(args);
    }

    private final CompositeDisposable disposable = new CompositeDisposable();

    private final ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();

    private final GameController controller = new GameController(pool);

    @Override
    public void start(final Stage stage) throws Exception {
        LOG.log(System.Logger.Level.DEBUG, "Displaying initial stage");

        final var loader = new FXMLLoader();
        loader.setController(controller);
        final Pane parent = loader.load(GameApplication.class.getResourceAsStream("/Main.fxml"));

        final var scene = new Scene(parent);
        stage.setScene(scene);

        final var keysPressed = Observable.<KeyEvent>create(emitter -> {
            final EventHandler<KeyEvent> listener = emitter::onNext;
            emitter.setCancellable(() -> scene.removeEventHandler(KeyEvent.KEY_PRESSED, listener));
            scene.addEventHandler(KeyEvent.KEY_PRESSED, listener);
        }).share();

        disposable.add(keysPressed.map(KeyEvent::getCode).filter(c -> c == KeyCode.ESCAPE).subscribe(k -> Platform.exit()));

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

        controller.start();
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        controller.stop();
        pool.shutdownNow();
        disposable.dispose();
    }
}
