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

    // Which mode F11 switches to when the game is windowed. Configurable
    // through the "F11 Mode" option in the video settings menu
    // (Borderless Window section).
    private static ScreenMode f11Target = ScreenMode.BORDERLESS;

    // Size/position of the window in WINDOWED mode, so we can restore it.
    private static int savedX, savedY, savedWidth, savedHeight;
    private static boolean hasSavedWindowedState = false;

    private BorderlessHandler() {
    }

    public static ScreenMode getCurrentMode() {
        return currentMode;
    }

    public static ScreenMode getF11Target() {
        return f11Target;
    }

    public static void setF11Target(ScreenMode target) {
        // Only BORDERLESS or FULLSCREEN make sense as an F11 target.
        if (target != ScreenMode.WINDOWED) {
            f11Target = target;
            BorderlessConfigFile.save(currentMode, f11Target);
        }
    }

    /**
     * Called exactly once at game startup (from FMLClientSetupEvent).
     * <p>
     * 1. Syncs the internal state with reality: if options.txt had
     *    fullscreen=true, vanilla already opened the window in exclusive
     *    fullscreen BEFORE our code ran -- we detect that by asking GLFW
     *    whether the window has a monitor assigned.
     * 2. Applies the mode saved in config/borderlesswindow.json.
     */
    public static void initializeFromConfig(Window window) {
        var config = BorderlessConfigFile.load();
        f11Target = config.f11Mode();

        if (glfwGetWindowMonitor(window.getWindow()) != 0L) {
            // The game booted in exclusive fullscreen via options.txt.
            currentMode = ScreenMode.FULLSCREEN;
        }

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

        boolean applied = switch (mode) {
            case WINDOWED -> applyWindowed(handle);
            case BORDERLESS -> applyBorderless(handle);
            case FULLSCREEN -> applyExclusiveFullscreen(handle);
        };

        if (!applied) {
            // The target monitor was in an invalid state (e.g. disconnected
            // mid-switch). We left the window untouched, so the internal
            // state must not change either.
            return;
        }

        currentMode = mode;
        BorderlessConfigFile.save(currentMode, f11Target);
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
            // Monitor in an invalid state (e.g., being disconnected right
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