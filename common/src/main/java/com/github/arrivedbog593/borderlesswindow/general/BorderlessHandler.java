package com.github.arrivedbog593.borderlesswindow.general;

import com.github.arrivedbog593.borderlesswindow.camera.BobbingState;
import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.github.arrivedbog593.borderlesswindow.camera.FovState;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelState;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayState;
import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.MinimizedRenderState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.github.arrivedbog593.borderlesswindow.visual.CloudState;
import com.github.arrivedbog593.borderlesswindow.visual.FogState;
import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectState;
import com.github.arrivedbog593.borderlesswindow.visual.SkyState;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.util.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.*;

/**
 * GLFW logic for the 3 screen modes:
 * <p>
 * - WINDOWED: normal decorated window, restored to its previous size.
 * - BORDERLESS: undecorated window stretched to the monitor size
 *   (+1 px of height on Windows, see applyBorderless), but it REMAINS a
 *   window (glfwSetWindowMonitor with monitor=NULL). Windows never changes
 *   the physical display mode, so there are no scaling/HDR conflicts.
 * - FULLSCREEN: real EXCLUSIVE fullscreen, same as vanilla
 *   (glfwSetWindowMonitor with an actual monitor handle), at the
 *   Fullscreen Resolution picked in the video settings, or the desktop's.
 */
public final class BorderlessHandler {

    private static ScreenMode currentMode = ScreenMode.WINDOWED;

    // Watch over the Fullscreen resolution right after it is applied, see
    // checkFullscreenMode. The fallback was seen within half a second.
    private static final long FULLSCREEN_CHECK_MILLIS = 2000L;
    private static final int FULLSCREEN_MAX_RETRIES = 2;
    private static VideoMode appliedFullscreenMode;
    private static long fullscreenCheckDeadline;
    private static int fullscreenRetriesLeft;

    // Minimize an exclusive window that Windows left on screen after
    // Alt+Tab, see checkStrandedFullscreen.
    private static final long STRANDED_MILLIS = 500L;
    private static final int STRANDED_MAX_ICONIFIES = 3;
    private static final long NOT_STRANDED = -1L;
    private static boolean focusKnown;
    private static boolean wasFocused;
    private static boolean watchingStranded;
    private static long strandedSince = NOT_STRANDED;
    private static int strandedIconifiesLeft;

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

    /**
     * Whether the window is our Borderless one, 1 px taller than the
     * monitor (applyBorderless). The game then renders 1 px shorter than
     * the window, matching the monitor, and presents that frame at the top
     * of the window, leaving the extra row hidden below the screen (see
     * WindowMixin and GlCommandEncoderMixin), as Minecraft 26.2 does for its
     * own non-exclusive fullscreen. Windows only: elsewhere a window
     * manager may clamp the window to the monitor, and trimming would cut a
     * visible row.
     */
    public static boolean hasHiddenBottomRow() {
        return Util.getPlatform() == Util.OS.WINDOWS && currentMode == ScreenMode.BORDERLESS;
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
     *    hands the FPS overlay, fog, cloud, screen overlay, bobbing, and
     *    enchantment numeral settings from the same file to their state
     *    holders (single load point for the whole config).
     * 3. Marks the handler as initialized, which unblocks F11 handling
     *    in WindowMixin.
     */
    public static void initializeFromConfig(Window window) {
        var config = BorderlessConfigFile.load();
        f11Target = config.f11Mode();
        FpsOverlayState.init(config.fpsOverlayMode(), config.fpsOverlayPosition());
        UnfocusedFpsState.init(config.unfocusedFpsLimit());
        UnfocusedVolumeState.init(config.unfocusedVolume());
        MinimizedRenderState.init(config.minimizedSkipRender());
        IdleFpsState.init(config.idleFpsDelay(), config.idleFpsLimit(),
                config.idleFpsSecondDelay(), config.idleFpsSecondLimit());
        FogState.init(config.terrainFog(), config.waterFog(),
                config.lavaFog(), config.powderSnowFog(),
                config.fogTerrainStart());
        SkyState.init(config.sunMoon(), config.stars(), config.weather());
        HudState.init(config.heldItemName());
        CloudState.init(config.cloudHeight());
        ScreenEffectState.init(config.waterOverlay(), config.fireOverlay(),
                config.smartFire(), config.pumpkinOverlay(),
                config.freezeOverlay(), config.portalOverlay(),
                config.spyglassOverlay());
        BobbingState.init(config.cameraBobMode(), config.handBob());
        EnchantmentLevelState.init(config.enchantmentLevelStyle());
        DistortionState.init(config.distortionPortal(), config.distortionNausea());
        FovState.init(config.fovSpeed(), config.fovFlying(),
                config.fovBow(), config.fovFluid());
        DistanceState.init(config.distanceItems(), config.distanceOrbs(),
                config.distanceArmorStands(), config.distanceParticles());
        ZoomState.init(config.zoomMode(), config.zoomLevel(),
                config.zoomScroll(), config.zoomSmooth(),
                config.zoomHideHand(), config.zoomHideHud());

        if (glfwGetWindowMonitor(window.handle()) != 0L) {
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

        long handle = window.handle();

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
        // rebuilt screen (e.g., our config screen) reads getCurrentMode() at
        // that exact moment. If we updated the state after applying, those
        // observers would rebuild with the stale mode (off-by-one UI bug).
        ScreenMode previous = currentMode;
        currentMode = mode;

        boolean applied = switch (mode) {
            case WINDOWED -> applyWindowed(handle);
            case BORDERLESS -> applyBorderless(handle);
            case FULLSCREEN -> applyExclusiveFullscreen(window, handle);
        };

        if (!applied) {
            // The target monitor was in an invalid state (e.g., disconnected
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
            // No previous windowed state to restore (e.g., the game booted
            // straight into borderless/fullscreen): use a sane default.
            glfwSetWindowMonitor(handle, 0L, 100, 100, 1280, 720, GLFW_DONT_CARE);
        }
        return true;
    }

    /**
     * Re-applies Fullscreen after the Fullscreen Resolution changed, from
     * vanilla's video settings or Sodium's (both end in
     * Window.changeFullscreenVideoMode, see WindowMixin). In the other
     * modes there is nothing to redo: the new resolution is used the next
     * time Fullscreen is entered.
     */
    public static void applyFullscreenResolution(Window window) {
        if (!initialized || currentMode != ScreenMode.FULLSCREEN) {
            return;
        }
        // Straight from one mode to the other, like vanilla: no detour
        // through the desktop's mode, so one display mode change instead of
        // two. Windows may still undo it, which checkFullscreenMode catches.
        applyExclusiveFullscreen(window, window.handle());
    }

    private static boolean applyBorderless(long handle) {
        long monitor = findWindowMonitor(handle);
        if (glfwGetVideoMode(monitor) == null) {
            // Monitor in an invalid state (e.g., being disconnected right
            // now). Better to leave the window as-is than to crash.
            return false;
        }
        int[] monitorX = new int[1], monitorY = new int[1];
        glfwGetMonitorPos(monitor, monitorX, monitorY);

        glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_FALSE);

        // Coming from Fullscreen at a lower resolution, the monitor still
        // reports that one until the window lets go of it.
        leaveExclusiveFullscreen(handle);
        GLFWVidMode vidMode = glfwGetVideoMode(monitor);
        if (vidMode == null) {
            return false;
        }

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
        // Windows only, like the promotion it defeats: elsewhere the row
        // would just be lost below the screen (hasHiddenBottomRow keeps
        // the frame aligned on Windows alone).
        int extraRow = Util.getPlatform() == Util.OS.WINDOWS ? 1 : 0;
        glfwSetWindowMonitor(handle, 0L, monitorX[0], monitorY[0],
                vidMode.width(), vidMode.height() + extraRow, GLFW_DONT_CARE);
        return true;
    }

    private static boolean applyExclusiveFullscreen(Window window, long handle) {
        long monitor = glfwGetWindowMonitor(handle);
        boolean alreadyExclusive = monitor != 0L;
        if (!alreadyExclusive) {
            monitor = findWindowMonitor(handle);
        }
        if (glfwGetVideoMode(monitor) == null) {
            // Monitor in an invalid state: leave the window untouched.
            return false;
        }

        // The resolution picked in the video settings (vanilla's or
        // Sodium's, both stored in Window), matched against the monitor's
        // modes the way vanilla does; "Current", or a mode this monitor
        // lacks, falls back to the desktop's.
        VideoMode videoMode = desktopMonitor(window, monitor, alreadyExclusive)
                .getPreferredVidMode(window.getPreferredFullscreenVideoMode());

        // Real exclusive fullscreen: an actual monitor handle is passed.
        // Decoration does not matter here, GLFW ignores it in exclusive mode.
        glfwSetWindowMonitor(handle, monitor, 0, 0,
                videoMode.getWidth(), videoMode.getHeight(), videoMode.getRefreshRate());

        appliedFullscreenMode = videoMode;
        fullscreenCheckDeadline = Util.getMillis() + FULLSCREEN_CHECK_MILLIS;
        fullscreenRetriesLeft = FULLSCREEN_MAX_RETRIES;
        return true;
    }

    /**
     * Called every frame (MinecraftMixin). Alt+Tab out of exclusive
     * Fullscreen could leave the game on screen: GLFW minimizes an
     * exclusive window when it loses focus (GLFW_AUTO_ICONIFY) and gives
     * the desktop its mode back, but Windows showed the window again about
     * 100 ms later, without giving it the focus, and GLFW then took the
     * monitor back at the game's resolution. The game stayed in front,
     * unfocused, until clicked. Seen on Windows 11 with NVIDIA and HDR,
     * when the monitor has to change modes on the way out (a Fullscreen
     * Resolution other than the desktop's), and in vanilla as well; logged
     * with GLFW's own focus and iconify callbacks.
     * <p>
     * With auto-iconify on, an exclusive window that has no focus and is
     * not minimized is never meant to last, so once that has held for
     * {@link #STRANDED_MILLIS} after the window lost focus, it is minimized
     * again, as GLFW meant to. Only after a focus loss -- a window that
     * never had the focus, such as one still starting up behind another,
     * is left alone -- and at most a few times per focus loss, in case
     * Windows keeps showing it again. Windowed and Borderless are not
     * affected: GLFW only minimizes an exclusive window.
     */
    public static void checkStrandedFullscreen(Window window) {
        long handle = window.handle();
        boolean focused = glfwGetWindowAttrib(handle, GLFW_FOCUSED) == GLFW_TRUE;
        if (!focusKnown) {
            // First frame: whatever the focus is now was not lost.
            focusKnown = true;
            wasFocused = focused;
            return;
        }
        if (focused != wasFocused) {
            wasFocused = focused;
            watchingStranded = !focused;
            strandedSince = NOT_STRANDED;
            strandedIconifiesLeft = STRANDED_MAX_ICONIFIES;
        }
        if (!watchingStranded) {
            return;
        }
        if (glfwGetWindowMonitor(handle) == 0L
                || glfwGetWindowAttrib(handle, GLFW_ICONIFIED) == GLFW_TRUE
                || glfwGetWindowAttrib(handle, GLFW_AUTO_ICONIFY) != GLFW_TRUE) {
            // Not exclusive, minimized as it should be, or another mod
            // turned auto-iconify off on purpose.
            strandedSince = NOT_STRANDED;
            return;
        }
        long now = Util.getMillis();
        if (strandedSince == NOT_STRANDED) {
            strandedSince = now;
            return;
        }
        if (now - strandedSince < STRANDED_MILLIS) {
            return;
        }
        strandedSince = NOT_STRANDED;
        if (strandedIconifiesLeft-- <= 0) {
            watchingStranded = false;
            return;
        }
        glfwIconifyWindow(handle);
    }

    /**
     * Called every frame (MinecraftMixin). For a short while after
     * Fullscreen is applied, re-applies it if the monitor fell back to
     * another mode. Going from one non-desktop resolution straight to
     * another, Windows sets the new mode and, before the next frame, puts
     * the desktop's back (seen on NVIDIA, with and without HDR, logged
     * with GLFW's own glfwGetVideoMode). From the desktop's mode the same
     * call sticks, so one retry is normally enough. Alt+Tab used to be
     * the workaround: GLFW sets the mode again when the window comes back.
     */
    public static void checkFullscreenMode(Window window) {
        if (appliedFullscreenMode == null) {
            return;
        }
        long handle = window.handle();
        long monitor = glfwGetWindowMonitor(handle);
        if (currentMode != ScreenMode.FULLSCREEN || monitor == 0L
                || glfwGetWindowAttrib(handle, GLFW_ICONIFIED) == GLFW_TRUE
                || Util.getMillis() > fullscreenCheckDeadline) {
            // Left Fullscreen, minimized (GLFW gives the desktop its mode
            // back on purpose) or settled: stop watching.
            appliedFullscreenMode = null;
            return;
        }

        GLFWVidMode actual = glfwGetVideoMode(monitor);
        VideoMode wanted = appliedFullscreenMode;
        if (actual == null
                || (actual.width() == wanted.getWidth() && actual.height() == wanted.getHeight())) {
            return;
        }
        if (fullscreenRetriesLeft-- <= 0) {
            appliedFullscreenMode = null;
            return;
        }
        glfwSetWindowMonitor(handle, monitor, 0, 0,
                wanted.getWidth(), wanted.getHeight(), wanted.getRefreshRate());
    }

    /**
     * The monitor's video modes, with the desktop's as its current one
     * (what "Current" means). Outside exclusive fullscreen a fresh query
     * gives exactly that. Inside it, the monitor reports the mode the game
     * set, so this takes vanilla's own Monitor instead (ScreenManager,
     * read when the monitor was connected): the one vanilla's slider lists
     * and resolves "Current" against.
     */
    private static Monitor desktopMonitor(Window window, long monitor, boolean exclusive) {
        if (exclusive) {
            // In exclusive fullscreen this is the window's own monitor.
            Monitor known = window.findBestMonitor();
            if (known != null && known.getMonitor() == monitor) {
                return known;
            }
        }
        return new Monitor(monitor);
    }

    /**
     * Takes the window out of exclusive fullscreen, if it is in it, which
     * gives the monitor back its desktop resolution. Only a step towards
     * another mode: the caller places the window right after.
     */
    private static void leaveExclusiveFullscreen(long handle) {
        long monitor = glfwGetWindowMonitor(handle);
        if (monitor == 0L) {
            return;
        }
        int[] monitorX = new int[1], monitorY = new int[1];
        int[] width = new int[1], height = new int[1];
        glfwGetMonitorPos(monitor, monitorX, monitorY);
        glfwGetWindowSize(handle, width, height);
        glfwSetWindowMonitor(handle, 0L, monitorX[0], monitorY[0],
                width[0], height[0], GLFW_DONT_CARE);
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