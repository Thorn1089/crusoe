package com.atomiccomics.crusoe;

import javafx.scene.media.AudioClip;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

public final class AudioPlayer {

    private static final Function<String, String> AUDIO_CLIP_RESOLVER = filename -> "file://" + Paths.get(".", "media", filename).toAbsolutePath().toString();

    private void handleWallBuilt(final Event<WallBuilt> event) {
        final var clip = new AudioClip(AUDIO_CLIP_RESOLVER.apply("build_wall.wav"));
        clip.play();
    }

    private void handleWallDestroyed(final Event<WallDestroyed> event) {
        final var clip = new AudioClip(AUDIO_CLIP_RESOLVER.apply("destroy_wall.wav"));
        clip.play();
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            switch (event.name().value()) {
                case "WallBuilt" -> handleWallBuilt((Event<WallBuilt>)event);
                case "WallDestroyed" -> handleWallDestroyed((Event<WallDestroyed>)event);
            };
        }
    }

}
