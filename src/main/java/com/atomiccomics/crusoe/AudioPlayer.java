package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;
import javafx.scene.media.AudioClip;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class AudioPlayer {

    private static final Function<String, String> AUDIO_CLIP_RESOLVER = filename -> "file://" + Paths.get(".", "media", filename).toAbsolutePath().toString();

    private static final Map<String, AudioClip> EVENTS_TO_CLIPS = new HashMap<>();

    private void playClip(final String clipName) {
        final var clip = EVENTS_TO_CLIPS.computeIfAbsent(clipName, f -> new AudioClip(AUDIO_CLIP_RESOLVER.apply(f)));
        clip.stop();
        clip.play();
    }

    public void process(final List<Event<?>> batch) {
        for(final var event : batch) {
            Optional.ofNullable(switch(event.name().value()) {
                case "WallBuilt" -> "build_wall.wav";
                case "WallDestroyed" -> "destroy_wall.wav";
                case "PlayerMoved" -> "move.wav";
                default -> null;
            }).ifPresent(this::playClip);
        }
    }

}
