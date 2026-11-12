package com.github.arrivedbog593.borderlesswindow.camera;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * The zoom: its six settings, its key and its live state.
 * <p>
 * HOW IT ZOOMS. By narrowing the field of view at the very end of
 * Camera.calculateFov (see CameraMixin), never by touching the
 * render distance: the world is drawn exactly as far as before, only a
 * smaller slice of it fills the screen. Being the last step, it stacks
 * on top of everything else that moves the FOV -- vanilla's setting,
 * the FOV effects and their per-source sliders, NeoForge's ComputeFov
 * event -- instead of fighting any of them.
 * <p>
 * THE SPYGLASS WINS. While the player looks through one, the zoom steps
 * aside (multiplier back to 1, scroll wheel back to vanilla): the
 * spyglass is an item with its own zoom, which this mod never touches,
 * and stacking both would just be a stronger spyglass.
 * <p>
 * MOUSE. Turning speed is scaled by the same multiplier as the FOV (see
 * MouseHandlerMixin), so at 4x the view turns a quarter as fast and the
 * crosshair keeps moving across the world at the speed it did unzoomed
 * -- the same idea as the slower aim vanilla gives the spyglass.
 * <p>
 * The live part (key state, scroll level, animation) is per frame and
 * render thread only, so it needs no synchronisation. onFrame() runs at
 * the start of every frame, before the FOV is read.
 * <p>
 * Nothing changes until the key is pressed, so the game stays vanilla by
 * default whatever the settings say. The settings persist immediately;
 * the level slider follows the usual apply/persist split.
 */
public final class ZoomState {

    public static final int MIN_LEVEL = 2;
    public static final int MAX_LEVEL = 16;
    public static final int DEFAULT_LEVEL = 4;

    /** Bounds for the scroll wheel, wider than the slider's on purpose. */
    private static final double MIN_SCROLL_LEVEL = 1.0D;
    private static final double MAX_SCROLL_LEVEL = 50.0D;

    /** One scroll notch: 4x to 50x is about 11 notches, 4x to 1x 6. */
    private static final double SCROLL_STEP = 1.25D;

    /**
     * Speed of the smooth animation, per second. ln(2) / 0.05 halves the
     * remaining distance every 50 ms, the same easing vanilla gives its
     * own FOV changes (half the way per tick), but measured per frame so
     * it stays fluid at any frame rate.
     */
    private static final double SMOOTH_RATE = Math.log(2.0D) / 0.05D;

    private static final double SNAP_EPSILON = 1.0E-4D;

    /**
     * The Controls section the key sits in. Not registered here: each
     * loader does it its own way (RegisterKeyMappingsEvent on NeoForge,
     * vanilla's Category.register on Fabric, which Fabric API sorts).
     */
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath("borderlesswindow", "main"));

    /**
     * C by default, like OptiFine. Shares it with vanilla's "Save Hotbar
     * Activator", which only acts in creative and combined with a number
     * key -- rebindable in Controls either way. Registered by each loader
     * (RegisterKeyMappingsEvent on NeoForge, Fabric's key binding module).
     */
    public static final KeyMapping KEY = new KeyMapping("key.borderlesswindow.zoom",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);

    // --- Settings -------------------------------------------------------
    private static ZoomMode mode = ZoomMode.HOLD;
    private static int initialLevel = DEFAULT_LEVEL;
    private static boolean scrollEnabled = true;
    private static boolean smoothEnabled = true;
    private static boolean hideHand = true;
    private static boolean hideHud = false;

    // --- Live state -----------------------------------------------------
    private static boolean active = false;
    private static double level = DEFAULT_LEVEL;
    private static double scrollAccumulator = 0.0D;
    /** What the FOV is multiplied by right now: 1 unzoomed, 1/level zoomed. */
    private static double multiplier = 1.0D;
    private static long lastFrameNanos = 0L;

    private ZoomState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(ZoomMode loadedMode, int loadedLevel, boolean loadedScroll,
                            boolean loadedSmooth, boolean loadedHideHand, boolean loadedHideHud) {
        mode = loadedMode;
        initialLevel = sanitizeLevel(loadedLevel);
        scrollEnabled = loadedScroll;
        smoothEnabled = loadedSmooth;
        hideHand = loadedHideHand;
        hideHud = loadedHideHud;
    }

    public static ZoomMode getMode() {
        return mode;
    }

    public static void setMode(ZoomMode newMode) {
        mode = newMode;
        // A toggled zoom must not stay stuck on after switching to hold.
        active = false;
        persist();
    }

    public static int getInitialLevel() {
        return initialLevel;
    }

    /** Memory only -- the slider persists on release. */
    public static void applyInitialLevel(int newLevel) {
        initialLevel = sanitizeLevel(newLevel);
    }

    public static void setInitialLevel(int newLevel) {
        applyInitialLevel(newLevel);
        persist();
    }

    public static boolean isScrollEnabled() {
        return scrollEnabled;
    }

    public static void setScrollEnabled(boolean enabled) {
        scrollEnabled = enabled;
        persist();
    }

    public static boolean isSmoothEnabled() {
        return smoothEnabled;
    }

    public static void setSmoothEnabled(boolean enabled) {
        smoothEnabled = enabled;
        persist();
    }

    public static boolean isHideHandEnabled() {
        return hideHand;
    }

    public static void setHideHandEnabled(boolean enabled) {
        hideHand = enabled;
        persist();
    }

    public static boolean isHideHudEnabled() {
        return hideHud;
    }

    public static void setHideHudEnabled(boolean enabled) {
        hideHud = enabled;
        persist();
    }

    public static void persist() {
        BorderlessConfigFile.saveCurrent();
    }

    // --- Per frame ------------------------------------------------------

    /**
     * Reads the key and advances the animation. Called at the start of
     * every frame (GameRendererMixin), so a press shows on the very next
     * frame instead of waiting for a tick.
     */
    public static void onFrame() {
        long now = System.nanoTime();
        double seconds = lastFrameNanos == 0L ? 0.0D : (now - lastFrameNanos) / 1.0E9D;
        lastFrameNanos = now;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            // Left the world: start the next one unzoomed.
            active = false;
            multiplier = 1.0D;
            KEY.consumeClick();
            return;
        }

        boolean wasActive = active;
        if (mode == ZoomMode.HOLD) {
            active = KEY.isDown();
        } else {
            while (KEY.consumeClick()) {
                active = !active;
            }
        }
        if (active && !wasActive) {
            // Every zoom starts at the configured level; scrolling only
            // adjusts the current one.
            level = initialLevel;
            scrollAccumulator = 0.0D;
        }

        double target = isZooming(minecraft.player) ? 1.0D / level : 1.0D;
        if (!smoothEnabled) {
            multiplier = target;
        } else {
            multiplier += (target - multiplier) * (1.0D - Math.exp(-SMOOTH_RATE * seconds));
            if (Math.abs(target - multiplier) < SNAP_EPSILON) {
                multiplier = target;
            }
        }
    }

    /** The FOV multiplier for this frame: 1 when not zoomed. */
    public static double getFovMultiplier() {
        return multiplier;
    }

    /** Whether the hand should be skipped this frame. */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean shouldHideHand() {
        return hideHand && isZooming(Minecraft.getInstance().player);
    }

    /** Whether the HUD should be skipped this frame, as if F1 were on. */
    public static boolean shouldHideHud() {
        return hideHud && isZooming(Minecraft.getInstance().player);
    }

    /**
     * Turns one scroll event into level changes while zoomed. Returns
     * true when it used the event, so the hotbar does not move as well.
     * Accumulates like vanilla's hotbar scrolling, so touchpads that send
     * fractions still step one notch at a time.
     */
    public static boolean onScroll(double amount) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!scrollEnabled || minecraft.gui.screen() != null || !isZooming(minecraft.player)) {
            return false;
        }
        if (scrollAccumulator != 0.0D && Math.signum(amount) != Math.signum(scrollAccumulator)) {
            scrollAccumulator = 0.0D;
        }
        scrollAccumulator += amount;
        int notches = (int) scrollAccumulator;
        if (notches != 0) {
            scrollAccumulator -= notches;
            level = Math.clamp(level * Math.pow(SCROLL_STEP, notches),
                    MIN_SCROLL_LEVEL, MAX_SCROLL_LEVEL);
        }
        return true;
    }

    /** The key is active and no spyglass is in the way. */
    private static boolean isZooming(LocalPlayer player) {
        return active && player != null && !player.isScoping();
    }

    private static int sanitizeLevel(int value) {
        return Math.clamp(value, MIN_LEVEL, MAX_LEVEL);
    }
}
