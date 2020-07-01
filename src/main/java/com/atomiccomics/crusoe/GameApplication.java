package com.atomiccomics.crusoe;

import com.google.inject.Guice;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
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

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class GameApplication extends Application {

    private static final System.Logger LOG = System.getLogger(GameApplication.class.getName());

    public record ScreenSize(int width, int height) {

    }

    public static void main(final String... args) {
        LOG.log(System.Logger.Level.INFO, "Starting JavaFX application");
        Application.launch(args);
    }

    private final CompositeDisposable disposable = new CompositeDisposable();
    private final Set<Runnable> tearDownHooks = new HashSet<>();

    @Override
    public void start(final Stage stage) throws Exception {
        final var injector = Guice.createInjector(new MainModule());

        final var engine = injector.getInstance(Engine.class);

        try(final var result = new ClassGraph().enableAllInfo().acceptPackages("com.atomiccomics.crusoe").scan()) {
            final ClassInfoList gameEngineComponents = result.getClassesWithAnnotation(RegisteredComponent.class.getName());
            for(final ClassInfo componentInfo : gameEngineComponents) {
                LOG.log(System.Logger.Level.TRACE, "Handler class: " + componentInfo);
                if(componentInfo.implementsInterface(Component.class.getName())) {
                    final var component = (Component)injector.getInstance(componentInfo.loadClass());
                    engine.register(component);
                } else {
                    final var component = Component.wrap(injector.getInstance(componentInfo.loadClass()));
                    engine.register(component);
                }
            }

            final ClassInfoList tearDownComponents = result.getClassesWithMethodAnnotation(Cleanup.class.getName());
            for(final ClassInfo tearDownInfo : tearDownComponents) {
                LOG.log(System.Logger.Level.TRACE, "Class with cleanup methods: " + tearDownInfo);
                final var type = tearDownInfo.loadClass();
                final var component = injector.getInstance(type);
                Stream.of(type.getMethods())
                        .filter(m -> m.isAnnotationPresent(Cleanup.class))
                        .forEach(m -> {
                            final Runnable hook = () -> {
                                try {
                                    m.invoke(component);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    LOG.log(System.Logger.Level.ERROR, "Unable to invoke cleanup hook", e);
                                }
                            };
                            tearDownHooks.add(hook);
                        });
            }
        }


        LOG.log(System.Logger.Level.DEBUG, "Displaying initial stage");

        final var loader = new FXMLLoader();
        final var controller = injector.getInstance(GameController.class);
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
        disposable.dispose();
        tearDownHooks.forEach(Runnable::run);
    }
}
