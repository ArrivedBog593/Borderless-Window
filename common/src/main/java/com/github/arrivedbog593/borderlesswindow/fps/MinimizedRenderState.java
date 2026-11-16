package com.github.arrivedbog593.borderlesswindow.fps;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Holds the "skip rendering while minimized" toggle. MinecraftMixin
 * consults it every frame: while the window is minimized it caps the loop
 * at {@link #MINIMIZED_FPS} and, after a short grace period (see
 * shouldSkipRendering), skips GameRenderer.render().
 * <p>
 * WHY BOTH. Skipping the draw alone would leave runTick spinning as fast
 * as the CPU allows -- a minimized window has nothing to vsync against --
 * so the frame rate is capped too. Skipping only the draw keeps everything
 * else in runTick running: client ticks, network packets, sound, the
 * integrated server. Only the picture stops, and it resumes on the first
 * frame after the window is restored.
 * <p>
 * WHY 10 FPS. runTick runs at most 10 client ticks per frame and drops the
 * rest, so the loop needs at least 2 frames per second to keep the 20
 * ticks per second the game expects; 10 leaves ample headroom. It is also
 * what vanilla 1.21.2+ uses for a minimized window -- drop this feature
 * along with UnfocusedFpsState when porting.
 * <p>
 * Minimized is read straight from GLFW (GLFW_ICONIFIED) each frame rather
 * than tracked through an iconify callback: vanilla installs none, and
 * installing ours could silently replace another mod's.
 */
public final class MinimizedRenderState {

    public static final int MINIMIZED_FPS = 10;

    /** How long the window stays minimized before drawing stops. */
    private static final long GRACE_MILLIS = 1000L;
    private static final long NOT_MINIMIZED = -1L;

    private static boolean enabled = false;

    /** When the window was minimized (Util.getMillis), or NOT_MINIMIZED. */
    private static long minimizedSince = NOT_MINIMIZED;

    private MinimizedRenderState() {
    }

    /** Called once at startup with the value loaded from disk. */
    public static void init(boolean loadedEnabled) {
        enabled = loadedEnabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        BorderlessConfigFile.saveCurrent();
    }

    /**
     * Whether the loop should be capped at {@link #MINIMIZED_FPS}: from the
     * moment the window is minimized. The toggle is checked first, so the
     * default never calls GLFW.
     */
    public static boolean shouldCapFrameRate() {
        if (!enabled) {
            minimizedSince = NOT_MINIMIZED;
            return false;
        }
        long handle = Minecraft.getInstance().getWindow().getWindow();
        if (GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) != GLFW.GLFW_TRUE) {
            minimizedSince = NOT_MINIMIZED;
            return false;
        }
        if (minimizedSince == NOT_MINIMIZED) {
            minimizedSince = Util.getMillis();
        }
        return true;
    }

    /**
     * Whether this frame should skip drawing: only once the window has
     * been minimized for {@link #GRACE_MILLIS}, and never while a loading
     * overlay is up.
     * <p>
     * WHY WAIT. GameRenderer.render does more than draw. It is where
     * vanilla pauses a single-player world 500 ms after the window loses
     * focus, and minimizing with Win+D or the title bar button takes the
     * focus away at the same instant: skipping right away meant the world
     * never paused and kept running unattended. One second of frames, at
     * 10 FPS, lets vanilla's own check run first.
     * <p>
     * WHY NOT WITH AN OVERLAY. A resource reload finishes inside the
     * LoadingOverlay's render; skipped, it would wait for the window to be
     * restored.
     */
    public static boolean shouldSkipRendering() {
        if (!shouldCapFrameRate()) {
            return false;
        }
        return Minecraft.getInstance().getOverlay() == null
                && Util.getMillis() - minimizedSince >= GRACE_MILLIS;
    }
}
