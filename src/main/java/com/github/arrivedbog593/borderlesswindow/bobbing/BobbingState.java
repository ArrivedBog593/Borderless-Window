package com.github.arrivedbog593.borderlesswindow.bobbing;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Holds the two bobbing settings, mirroring the role FogState and
 * ScreenEffectState play for their features. Both UIs read and write
 * through this class, and GameRendererMixin consults it on every frame
 * that vanilla would apply bobbing.
 * <p>
 * IMPORTANT -- these settings REFINE vanilla's own "View Bobbing"
 * option, they do not replace it. Vanilla guards both bobView call
 * sites with options.bobView(), so if the player turns View Bobbing off
 * in Minecraft's video settings, bobView never runs and nothing here
 * has any effect. Both tooltips say so; without that note the first bug
 * report is guaranteed to be "your bobbing options do nothing".
 * <p>
 * Defaults are vanilla behavior (FULL camera, hand bobbing on), so
 * updating the mod never changes how the game looks until the player
 * asks for it.
 * <p>
 * Every setter persists immediately to config/borderlesswindow.json.
 * init() is the only entry that does NOT save: it is called once at
 * startup by BorderlessHandler.initializeFromConfig() with the values
 * just loaded from disk.
 */
public final class BobbingState {

    private static CameraBobMode cameraBobMode = CameraBobMode.FULL;
    private static boolean handBob = true;

    private BobbingState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(CameraBobMode loadedMode, boolean loadedHandBob) {
        cameraBobMode = loadedMode;
        handBob = loadedHandBob;
    }

    public static CameraBobMode getCameraBobMode() {
        return cameraBobMode;
    }

    public static void setCameraBobMode(CameraBobMode mode) {
        cameraBobMode = mode;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isHandBobEnabled() {
        return handBob;
    }

    public static void setHandBobEnabled(boolean enabled) {
        handBob = enabled;
        BorderlessConfigFile.saveCurrent();
    }
}