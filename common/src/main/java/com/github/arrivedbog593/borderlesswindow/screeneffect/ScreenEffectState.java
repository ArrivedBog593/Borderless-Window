package com.github.arrivedbog593.borderlesswindow.screeneffect;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Holds the screen overlay toggles, mirroring the role FogState plays for
 * fog. Both UIs (the vanilla config screen and the Sodium video settings
 * integration) read and write through this class.
 * <p>
 * TWO SOURCES, ONE SECTION. The water and fire overlays come from
 * ScreenEffectRenderer and are removed through a NeoForge event
 * (ScreenEffectHandler, no mixin). The pumpkin, freeze, portal and
 * spyglass overlays come from Gui and need bytecode (GuiMixin), because
 * NeoForge exposes them as a single GUI layer that cannot be cancelled
 * one by one. They live in the same holder and the same user-facing
 * section anyway: to the player they are all "things drawn over my
 * view", and splitting them by which vanilla class happens to own them
 * would be organising the UI around our implementation.
 * <p>
 * NAMING: "screen effect" rather than "overlay" on purpose -- this mod
 * already has an FPS *overlay*, and reusing the word for two unrelated
 * things in the same codebase gets confusing fast. The user-facing
 * strings still say "overlay", which is what players call it.
 * <p>
 * POLARITY: every toggle follows the fog convention -- true = vanilla
 * behavior (shown), false = removed, default true. smartFire is the odd
 * one out: it ADDS behavior rather than removing it, so its default is
 * false and true means "hide the fire overlay, but only while Fire
 * Resistance is active".
 * <p>
 * Every setter persists immediately to config/borderlesswindow.json.
 * init() is the only entry that does NOT save: it is called once at
 * startup by BorderlessHandler.initializeFromConfig() with the values
 * just loaded from disk.
 */
public final class ScreenEffectState {

    // --- ScreenEffectRenderer (event-driven, see ScreenEffectHandler)
    private static boolean waterOverlay = true;
    private static boolean fireOverlay = true;
    private static boolean smartFire = false;

    // --- Gui camera overlays (see GuiMixin)
    private static boolean pumpkinOverlay = true;
    private static boolean freezeOverlay = true;
    private static boolean portalOverlay = true;
    private static boolean spyglassOverlay = true;

    private ScreenEffectState() {
    }

    /**
     * Called once at startup with the values loaded from disk.
     * <p>
     * NOTE: seven positional booleans is past the point where a caller
     * can get them right by reading the call site. There is exactly one
     * caller (BorderlessHandler.initializeFromConfig), so the risk is
     * contained, but if an eighth toggle lands here this should take
     * the LoadedConfig record instead.
     */
    public static void init(boolean loadedWater, boolean loadedFire, boolean loadedSmartFire,
                            boolean loadedPumpkin, boolean loadedFreeze,
                            boolean loadedPortal, boolean loadedSpyglass) {
        waterOverlay = loadedWater;
        fireOverlay = loadedFire;
        smartFire = loadedSmartFire;
        pumpkinOverlay = loadedPumpkin;
        freezeOverlay = loadedFreeze;
        portalOverlay = loadedPortal;
        spyglassOverlay = loadedSpyglass;
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

    public static boolean isPumpkinOverlayEnabled() {
        return pumpkinOverlay;
    }

    public static void setPumpkinOverlayEnabled(boolean enabled) {
        pumpkinOverlay = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isFreezeOverlayEnabled() {
        return freezeOverlay;
    }

    public static void setFreezeOverlayEnabled(boolean enabled) {
        freezeOverlay = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isPortalOverlayEnabled() {
        return portalOverlay;
    }

    public static void setPortalOverlayEnabled(boolean enabled) {
        portalOverlay = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isSpyglassOverlayEnabled() {
        return spyglassOverlay;
    }

    public static void setSpyglassOverlayEnabled(boolean enabled) {
        spyglassOverlay = enabled;
        BorderlessConfigFile.saveCurrent();
    }
}