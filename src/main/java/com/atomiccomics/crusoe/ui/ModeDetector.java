package com.atomiccomics.crusoe.ui;

import com.atomiccomics.crusoe.*;
import com.google.inject.Singleton;

@Singleton
@RegisteredComponent
public final class ModeDetector {

    public enum InputMode {
        COMMAND,
        BUILD
    }

    private volatile InputMode mode;

    @Handler(PlayerSelected.class)
    public void handlePlayerSelected(final PlayerSelected event) {
        mode = InputMode.COMMAND;
    }

    @Handler(PlayerDeselected.class)
    public void handlePlayerDeselected(final PlayerDeselected event) {
        mode = null;
    }

    @Handler(WallBlueprintActivated.class)
    public void handleWallBlueprintActivated(final WallBlueprintActivated event) {
        mode = InputMode.BUILD;
    }

    @Handler(WallBlueprintDeactivated.class)
    public void handleWallBlueprintDeactivated(final WallBlueprintDeactivated event) {
        mode = null;
    }

    public InputMode currentMode() {
        return mode;
    }

}
