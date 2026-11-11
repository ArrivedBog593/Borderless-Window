package com.github.arrivedbog593.borderlesswindow.fps;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Holds the "skip rendering while minimized" toggle. MinecraftMixin and
 * FramerateLimitTrackerMixin consult it every frame: while the window is
 * minimized the first skips drawing the frame and the second caps the
 * loop at {@link #MINIMIZED_FPS}.
 * <p>
 * WHAT VANILLA ALREADY DOES (1.21.2+): a minimized window is capped at 10
 * FPS and the finished frame is not copied to the screen, but the whole
 * frame is still drawn first. Skipping the draw is the part only this
 * option adds; the cap is the same as vanilla's and simply stacks.
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
 * what vanilla uses for a minimized window.
 * <p>
 * Minimized is read straight from GLFW (GLFW_ICONIFIED) each frame rather
 * than tracked through an iconify callback: vanilla installs none, and
 * installing ours could silently replace another mod's.
 */
public final class MinimizedRenderState {

    public static final int MINIMIZED_FPS = 10;

    private static boolean enabled = false;

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

    /** Whether this frame should skip drawing. The toggle is checked first, so the default never calls GLFW. */
    public static boolean shouldSkipRendering() {
        if (!enabled) {
            return false;
        }
        long handle = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
    }
}
