package com.github.arrivedbog593.borderlesswindow.screeneffect;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Holds the screen overlay toggles, mirroring the role FogState plays for
 * fog. Both UIs (the vanilla config screen and the Sodium video settings
 * integration) read and write through this class, and ScreenEffectHandler
 * consults it every time the game is about to draw one of these overlays.
 * <p>
 * NAMING: "screen effect" rather than "overlay" on purpose -- this mod
 * already has an FPS *overlay*, and reusing the word for two unrelated
 * things in the same codebase gets confusing fast. It also matches the
 * vanilla class these come from (ScreenEffectRenderer) and the NeoForge
 * event that exposes them (RenderBlockScreenEffectEvent). The
 * user-facing strings still say "overlay", which is what players call it.
 * <p>
 * POLARITY: waterOverlay/fireOverlay follow the fog convention --
 * true = vanilla behavior (shown), false = removed, default true.
 * smartFire is the odd one out: it ADDS behavior rather than removing
 * it, so its default is false and true means "hide the fire overlay,
 * but only while Fire Resistance is active".
 * <p>
 * Every setter persists immediately to config/borderlesswindow.json.
 * init() is the only entry that does NOT save: it is called once at
 * startup by BorderlessHandler.initializeFromConfig() with the values
 * just loaded from disk.
 */
public final class ScreenEffectState {

    private static boolean waterOverlay = true;
    private static boolean fireOverlay = true;
    private static boolean smartFire = false;

    private ScreenEffectState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(boolean loadedWater, boolean loadedFire, boolean loadedSmartFire) {
        waterOverlay = loadedWater;
        fireOverlay = loadedFire;
        smartFire = loadedSmartFire;
    }

    public static boolean isWaterOverlayEnabled() {
        return waterOverlay;
    }

    public static void setWaterOverlayEnabled(boolean enabled) {
        waterOverlay = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isFireOverlayEnabled() {
        return fireOverlay;
    }

    public static void setFireOverlayEnabled(boolean enabled) {
        fireOverlay = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isSmartFireEnabled() {
        return smartFire;
    }

    public static void setSmartFireEnabled(boolean enabled) {
        smartFire = enabled;
        BorderlessConfigFile.saveCurrent();
    }
}