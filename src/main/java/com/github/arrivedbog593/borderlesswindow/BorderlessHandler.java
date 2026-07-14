package com.github.arrivedbog593.borderlesswindow;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.*;

/**
 * GLFW logic for the 3 screen modes:
 * <p>
 * - WINDOWED: normal decorated window, restored to its previous size.
 * - BORDERLESS: undecorated window stretched to the monitor size
 *   (+1 px of height, see applyBorderless), but it REMAINS a window
 *   (glfwSetWindowMonitor with monitor=NULL). Windows never changes the
 *   physical display mode, so there are no scaling/HDR conflicts.
 * - FULLSCREEN: real EXCLUSIVE fullscreen, same as vanilla
 *   (glfwSetWindowMonitor with an actual monitor handle).
 */
public final class BorderlessHandler {

    private static ScreenMode currentMode = ScreenMode.WINDOWED;

    // False until initializeFromConfig() has loaded the saved config.
    // Vanilla's Minecraft constructor calls toggleFullScreen() during early
    // boot (to reconcile options.txt's fullscreen flag) -- long before our
    // config is loaded. Toggles arriving before initialization must be
    // swallowed, otherwise they apply the default F11 target and SAVE it,
    // clobbering the user's saved config before we ever read it.
    private static boolean initialized = false;

    // What F11 does. Configurable through the "F11 Mode" option in the
    // video settings menu (Borderless Window section).
    private static F11Mode f11Target = F11Mode.BORDERLESS;

    // Size/position of the window in WINDOWED mode, so we can restore it.
    private static int savedX, savedY, savedWidth, savedHeight;
    private static boolean hasSavedWindowedState = false;

    private BorderlessHandler() {
    }

    public static ScreenMode getCurrentMode() {
        return currentMode;
    }

    public static F11Mode getF11Target() {
        return f11Target;
    }

    public static void setF11Target(F11Mode target) {
        f11Target = target;
        BorderlessConfigFile.saveCurrent();
    }

    /**
     * Computes which mode F11 should switch to from the current one,
     * according to the configured F11 mode:
     * - BORDERLESS / FULLSCREEN: toggle Windowed <-> that mode.
     * - CYCLE: Windowed -> Borderless -> Fullscreen -> Windowed.
     */
    public static ScreenMode getNextF11Mode() {
        return switch (f11Target) {
            case BORDERLESS -> currentMode == ScreenMode.WINDOWED
                    ? ScreenMode.BORDERLESS : ScreenMode.WINDOWED;
            case FULLSCREEN -> currentMode == ScreenMode.WINDOWED
                    ? ScreenMode.FULLSCREEN : ScreenMode.WINDOWED;
            case CYCLE -> switch (currentMode) {
                case WINDOWED -> ScreenMode.BORDERLESS;
                case BORDERLESS -> ScreenMode.FULLSCREEN;
                case FULLSCREEN -> ScreenMode.WINDOWED;
            };
        };
    }

    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Called exactly once at game startup (from FMLClientSetupEvent).
     * <p>
     * 1. Syncs the internal state with reality: if options.txt had
     *    fullscreen=true, vanilla may have CREATED the window in exclusive
     *    fullscreen -- we detect that by asking GLFW whether the window
     *    has a monitor assigned.
     * 2. Applies the mode saved in config/borderlesswindow.json, and
     *    hands the FPS overlay settings from the same file to
     *    FpsOverlayState (single load point for the whole config).
     * 3. Marks the handler as initialized, which unblocks F11 handling
     *    in WindowMixin.
     */
    public static void initializeFromConfig(Window window) {
        var config = BorderlessConfigFile.load();
        f11Target = config.f11Mode();
        FpsOverlayState.init(config.fpsOverlayMode(), config.fpsOverlayPosition());

        if (glfwGetWindowMonitor(window.getWindow()) != 0L) {
            // The game booted in exclusive fullscreen via options.txt.
            currentMode = ScreenMode.FULLSCREEN;
        }

        initialized = true;
        setMode(window, config.screenMode());
    }

    public static void setMode(Window window, ScreenMode mode) {
        if (mode == currentMode) {
            return;
        }

        long handle = window.getWindow();

        // If we are leaving WINDOWED, remember its size/position so we can
        // restore it later, regardless of which mode we switch to now.
        if (currentMode == ScreenMode.WINDOWED) {
            int[] x = new int[1], y = new int[1], w = new int[1], h = new int[1];
            glfwGetWindowPos(handle, x, y);
            glfwGetWindowSize(handle, w, h);
            savedX = x[0];
            savedY = y[0];
            savedWidth = w[0];
            savedHeight = h[0];
            hasSavedWindowedState = true;
        }

        // Update the state BEFORE applying the GLFW change: applying resizes
        // the window, which synchronously re-inits any open Screen -- and a
        // rebuilt screen (e.g. our config screen) reads getCurrentMode() at
        // that exact moment. If we updated the state after applying, those
        // observers would rebuild with the stale mode (off-by-one UI bug).
        ScreenMode previous = currentMode;
        currentMode = mode;

        boolean applied = switch (mode) {
            case WINDOWED -> applyWindowed(handle);
            case BORDERLESS -> applyBorderless(handle);
            case FULLSCREEN -> applyExclusiveFullscreen(handle);
        };

        if (!applied) {
            // The target monitor was in an invalid state (e.g. disconnected
            // mid-switch). The window was left untouched, so roll the
            // internal state back to match reality.
            currentMode = previous;
            return;
        }

        BorderlessConfigFile.saveCurrent();
    }

    private static boolean applyWindowed(long handle) {
        glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_TRUE);
        if (hasSavedWindowedState) {
            glfwSetWindowMonitor(handle, 0L, savedX, savedY, savedWidth, savedHeight, GLFW_DONT_CARE);
        } else {
            // No previous windowed state to restore (e.g. the game booted
            // straight into borderless/fullscreen): use a sane default.
            glfwSetWindowMonitor(handle, 0L, 100, 100, 1280, 720, GLFW_DONT_CARE);
        }
        return true;
    }

    private static boolean applyBorderless(long handle) {
        long monitor = findWindowMonitor(handle);
        GLFWVidMode vidMode = glfwGetVideoMode(monitor);
        if (vidMode == null) {
            // Monitor in an invalid state (e.g. being disconnected right
            // now). Better to leave the window as-is than to crash.
            return false;
        }
        int[] monitorX = new int[1], monitorY = new int[1];
        glfwGetMonitorPos(monitor, monitorX, monitorY);

        glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_FALSE);

        // ANTI-PROMOTION TRICK (+1 px of height):
        // If a borderless window covers the monitor EXACTLY, Windows and
        // the GPU driver (especially NVIDIA with OpenGL) "promote" it to
        // an exclusive-fullscreen-like presentation mode (independent
        // flip). With HDR enabled, that promotion causes the same black
        // flash / color profile switch as exclusive fullscreen -- exactly
        // what this mode is meant to avoid. Making the window 1 pixel
        // taller than the monitor defeats the detection; the extra pixel
        // hangs off-screen and is invisible. This is the same workaround
        // used by borderless tools such as Special K or Borderless Gaming.
        glfwSetWindowMonitor(handle, 0L, monitorX[0], monitorY[0],
                vidMode.width(), vidMode.height() + 1, GLFW_DONT_CARE);
        return true;
    }

    private static boolean applyExclusiveFullscreen(long handle) {
        long monitor = findWindowMonitor(handle);
        GLFWVidMode vidMode = glfwGetVideoMode(monitor);
        if (vidMode == null) {
            // Monitor in an invalid state: leave the window untouched.
            return false;
        }

        // Real exclusive fullscreen: an actual monitor handle is passed.
        // Decoration does not matter here, GLFW ignores it in exclusive mode.
        glfwSetWindowMonitor(handle, monitor, 0, 0,
                vidMode.width(), vidMode.height(), vidMode.refreshRate());
        return true;
    }

    /**
     * Returns the monitor the window is on RIGHT NOW (the one containing
     * its center), instead of always assuming the primary one. This way,
     * if you drag the window to your second monitor and enable
     * borderless/fullscreen, it is applied on THAT monitor. If the center
     * is not inside any monitor (rare case: window dragged outside all of
     * them), it falls back to the primary monitor.
     */
    private static long findWindowMonitor(long handle) {
        int[] wx = new int[1], wy = new int[1], ww = new int[1], wh = new int[1];
        glfwGetWindowPos(handle, wx, wy);
        glfwGetWindowSize(handle, ww, wh);
        int centerX = wx[0] + ww[0] / 2;
        int centerY = wy[0] + wh[0] / 2;

        PointerBuffer monitors = glfwGetMonitors();
        if (monitors != null) {
            for (int i = 0; i < monitors.limit(); i++) {
                long monitor = monitors.get(i);
                GLFWVidMode vidMode = glfwGetVideoMode(monitor);
                if (vidMode == null) {
                    continue;
                }
                int[] mx = new int[1], my = new int[1];
                glfwGetMonitorPos(monitor, mx, my);
                if (centerX >= mx[0] && centerX < mx[0] + vidMode.width()
                        && centerY >= my[0] && centerY < my[0] + vidMode.height()) {
                    return monitor;
                }
            }
        }
        return glfwGetPrimaryMonitor();
    }
}
