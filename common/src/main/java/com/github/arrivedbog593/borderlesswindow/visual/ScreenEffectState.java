package com.github.arrivedbog593.borderlesswindow.visual;

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
 * FIRE HEIGHT lowers the flames instead of removing them: at 100% they
 * are vanilla's, lower values push them down the screen (see
 * ScreenEffectRendererMixin). It applies whenever the flames are drawn,
 * so smart fire still hides them with Fire Resistance.
 * <p>
 * TOTEM ANIMATION is the item that pops up over the screen when a Totem
 * of Undying saves you (GameRenderer.displayItemActivation, see
 * GameRendererMixin). Mods that reuse that animation for their own items
 * are hidden with it. The totem's particles and sound are left alone.
 * <p>
 * Every setter persists immediately to config/borderlesswindow.json.
 * init() is the only entry that does NOT save: it is called once at
 * startup by BorderlessHandler.initializeFromConfig() with the values
 * just loaded from disk.
 */
public final class ScreenEffectState {

    public static final int MIN_FIRE_HEIGHT = 10;
    public static final int MAX_FIRE_HEIGHT = 100;
    public static final int FIRE_HEIGHT_STEP = 5;

    /**
     * How far above the bottom edge of the screen vanilla's flames reach,
     * in the units submitFire draws in. Each quad is 1 tall, centred 0.3
     * below the middle, so its top sits at 0.2. They are drawn 0.5 in
     * front of the camera through the hand's perspective projection
     * (GameRenderer.renderItemInHand, 70 degrees: Minecraft's fixed hand
     * FOV, not the player's setting), where the bottom edge of the screen
     * is at -0.5 * tan(35 degrees) = -0.35. So 0.55 of them shows.
     */
    private static final float VANILLA_FIRE_REACH = 0.55F;

    // --- ScreenEffectRenderer (event-driven, see ScreenEffectHandler)
    private static boolean waterOverlay = true;
    private static boolean fireOverlay = true;
    private static boolean smartFire = false;
    private static int fireHeight = MAX_FIRE_HEIGHT;

    // --- Gui camera overlays (see GuiMixin)
    private static boolean pumpkinOverlay = true;
    private static boolean freezeOverlay = true;
    private static boolean portalOverlay = true;
    private static boolean spyglassOverlay = true;

    // --- GameRenderer (see GameRendererMixin)
    private static boolean totemAnimation = true;

    private ScreenEffectState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(BorderlessConfigFile.LoadedConfig config) {
        waterOverlay = config.waterOverlay();
        fireOverlay = config.fireOverlay();
        smartFire = config.smartFire();
        fireHeight = sanitizeFireHeight(config.fireHeight());
        pumpkinOverlay = config.pumpkinOverlay();
        freezeOverlay = config.freezeOverlay();
        portalOverlay = config.portalOverlay();
        spyglassOverlay = config.spyglassOverlay();
        totemAnimation = config.totemAnimation();
    }

    public static int getFireHeight() {
        return fireHeight;
    }

    /** Memory only -- the slider persists on release. */
    public static void applyFireHeight(int percent) {
        fireHeight = sanitizeFireHeight(percent);
    }

    public static void setFireHeight(int percent) {
        applyFireHeight(percent);
        persist();
    }

    public static void persist() {
        BorderlessConfigFile.saveCurrent();
    }

    /**
     * How much lower than vanilla the flames are drawn, in submitFire's
     * units: 0 at 100%, the whole reach at 0%, so the top of the flames
     * sits at fireHeight% of vanilla's height above the bottom edge.
     */
    public static float fireDrop() {
        return VANILLA_FIRE_REACH * (MAX_FIRE_HEIGHT - fireHeight) / MAX_FIRE_HEIGHT;
    }

    private static int sanitizeFireHeight(int value) {
        return Math.clamp(value, MIN_FIRE_HEIGHT, MAX_FIRE_HEIGHT);
    }

    public static boolean isTotemAnimationEnabled() {
        return totemAnimation;
    }

    public static void setTotemAnimationEnabled(boolean enabled) {
        totemAnimation = enabled;
        BorderlessConfigFile.saveCurrent();
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