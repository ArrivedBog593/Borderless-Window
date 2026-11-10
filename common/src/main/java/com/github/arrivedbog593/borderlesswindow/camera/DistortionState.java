package com.github.arrivedbog593.borderlesswindow.camera;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Holds the two distortion percentages, splitting what vanilla exposes
 * as a single "Distortion Effects" slider into its two sources: the
 * Nether portal and the Nausea effect.
 * <p>
 * WHY A SPLIT IS POSSIBLE AT ALL. Since 1.21.11 vanilla keeps the two
 * apart: LocalPlayer.portalEffectIntensity is the portal alone, and
 * Nausea is read from the effect itself (getEffectBlendFactor).
 * GameRenderer.renderLevel takes the larger of the two, so
 * GameRendererMixin scales each one before that. (1.21.1 had a single
 * accumulator for both, and the source had to be inferred tick by tick.)
 * <p>
 * INSIDE A PORTAL, THE PORTAL RULES. While the player stands in a Nether
 * portal, Nausea's spin is scaled by the PORTAL percentage too: with
 * Portal at 0% the view stops spinning on entering the portal, Nausea or
 * not, and the Nausea spin comes back on stepping out. That is how 1.21.1
 * behaved -- its single accumulator was the portal's while inside one --
 * and it is kept on purpose, so each slider governs one place: the
 * portal, and everywhere else. "Inside" is contact with the portal block
 * (EntityMixin), not vanilla's portal intensity, which fades out while
 * the player stands in the destination portal on cooldown. The contact is
 * latched once per tick ({@link #latchPortalContact}), so every frame of
 * a tick agrees.
 * <p>
 * THE GREEN NAUSEA TEXTURE follows the Nausea slider on purpose. Vanilla
 * draws it under Nausea whenever Distortion Effects is below 100%, at
 * intensity (1 - scale): it is the static cue that replaces the spin as
 * the spin is turned down. GuiMixin folds the Nausea factor into the
 * scale that texture reads, so lowering the Nausea slider trades spin
 * for the green cue the same way Minecraft's own slider does -- at 0%
 * there is no spin and the texture is at full strength. Inside a portal
 * the texture reads vanilla's scale untouched: there the Nausea slider
 * does not speak.
 * <p>
 * SCALE: percentages, 0..100, default 100 (vanilla). The mixin squares
 * the factor before applying it, because vanilla squares its own scale
 * (f2 * f2) -- matching the curve means 50% here feels like 50% on
 * Minecraft's slider instead of feeling twice as strong.
 * <p>
 * HIERARCHY: both of these multiply vanilla's slider rather than
 * replacing it. With Distortion Effects at 0% in Minecraft's
 * accessibility settings nothing spins at all and neither of these does
 * anything, the same way the bobbing options sit under View Bobbing.
 * Both tooltips say so.
 * <p>
 * Split apply/persist like CloudState, for the same reason: these are
 * sliders, and AbstractSliderButton fires applyValue on every step of a
 * drag. Applying in memory gives a live preview; persist() runs once on
 * release.
 */
public final class DistortionState {

    public static final int MIN_PERCENT = 0;
    public static final int MAX_PERCENT = 100;
    public static final int DEFAULT_PERCENT = 100;

    private static int portalPercent = DEFAULT_PERCENT;
    private static int nauseaPercent = DEFAULT_PERCENT;

    /** Set by EntityMixin on every portal contact; read once per tick. Client thread only. */
    private static boolean touchingPortal = false;
    /** Whether the player was in a portal during the last tick. */
    private static boolean inPortal = false;

    private DistortionState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(int loadedPortal, int loadedNausea) {
        portalPercent = sanitize(loadedPortal);
        nauseaPercent = sanitize(loadedNausea);
    }

    public static int getPortalPercent() {
        return portalPercent;
    }

    public static int getNauseaPercent() {
        return nauseaPercent;
    }

    /** Memory only -- the change shows on the next frame. */
    public static void applyPortalPercent(int percent) {
        portalPercent = sanitize(percent);
    }

    /** Memory only -- the change shows on the next frame. */
    public static void applyNauseaPercent(int percent) {
        nauseaPercent = sanitize(percent);
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setPortalPercent(int percent) {
        applyPortalPercent(percent);
        persist();
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setNauseaPercent(int percent) {
        applyNauseaPercent(percent);
        persist();
    }

    /**
     * Writes the whole live config to disk. Cheap to call redundantly:
     * saveCurrent() always serializes the full state.
     */
    public static void persist() {
        BorderlessConfigFile.saveCurrent();
    }

    /** 0..100 as a 0..1 factor. The mixin squares it. */
    public static float getPortalFactor() {
        return portalPercent / 100.0F;
    }

    /** 0..100 as a 0..1 factor. The mixin squares it. */
    public static float getNauseaFactor() {
        return nauseaPercent / 100.0F;
    }

    /**
     * The factor for Nausea's spin: the portal's while inside a portal
     * (see INSIDE A PORTAL above), the Nausea slider's everywhere else.
     */
    public static float getNauseaSpinFactor() {
        return inPortal ? getPortalFactor() : getNauseaFactor();
    }

    public static boolean isInPortal() {
        return inPortal;
    }

    /** Called by EntityMixin whenever the player touches a Nether portal. */
    public static void markTouchingPortal() {
        touchingPortal = true;
    }

    /**
     * Turns the contacts recorded during the last tick into
     * {@link #isInPortal()}, and starts recording the next tick's.
     * Called at the start of every client tick (GameRenderer.tick runs
     * before the entities do).
     */
    public static void latchPortalContact() {
        inPortal = touchingPortal;
        touchingPortal = false;
    }

    private static int sanitize(int value) {
        if (value < MIN_PERCENT) {
            return MIN_PERCENT;
        }
        return Math.min(value, MAX_PERCENT);
    }
}
