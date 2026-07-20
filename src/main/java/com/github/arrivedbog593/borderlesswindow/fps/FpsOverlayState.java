package com.github.arrivedbog593.borderlesswindow.fps;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Holds the FPS overlay settings (mode + position), mirroring the role
 * BorderlessHandler plays for the screen mode. Both UIs (the vanilla
 * config screen and the Sodium video settings integration) read and
 * write through this class, so they always agree.
 * <p>
 * Every setter persists immediately to config/borderlesswindow.json --
 * same behavior as the screen mode options. init() is the only entry
 * that does NOT save: it is called once at startup by
 * BorderlessHandler.initializeFromConfig() with the values just loaded
 * from disk, and saving there would be redundant.
 */
public final class FpsOverlayState {

    private static FpsOverlayMode mode = FpsOverlayMode.OFF;
    private static FpsOverlayPosition position = FpsOverlayPosition.TOP_LEFT;

    private FpsOverlayState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(FpsOverlayMode loadedMode, FpsOverlayPosition loadedPosition) {
        mode = loadedMode;
        position = loadedPosition;
    }

    public static FpsOverlayMode getMode() {
        return mode;
    }

    public static void setMode(FpsOverlayMode newMode) {
        mode = newMode;
        BorderlessConfigFile.saveCurrent();
    }

    public static FpsOverlayPosition getPosition() {
        return position;
    }

    public static void setPosition(FpsOverlayPosition newPosition) {
        position = newPosition;
        BorderlessConfigFile.saveCurrent();
    }
}
