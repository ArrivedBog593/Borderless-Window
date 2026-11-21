package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import net.minecraft.util.Util;
import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_DisplayMode;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.IntBuffer;

/**
 * Reacts to focus changes, keeps a maximized window maximized across
 * fullscreen, and keeps exclusive Fullscreen on the chosen resolution,
 * with the game drawn at its size.
 * Entering and leaving the screen modes needs no other hook since 26.3:
 * vanilla's window handles all 3 itself (see BorderlessHandler), and F11
 * is handled in MinecraftMixin.
 */
@Mixin(Window.class)
public abstract class WindowMixin {

    // Watch over the Fullscreen resolution right after it is applied, see
    // checkFullscreenMode. The fallback was seen 1 to 2 seconds later, and
    // once came back after the first retry.
    @Unique
    private static final long FULLSCREEN_CHECK_MILLIS = 5000L;
    @Unique
    private static final int FULLSCREEN_MAX_RETRIES = 3;
    @Unique
    private long borderlesswindow$checkDeadline;
    @Unique
    private int borderlesswindow$retriesLeft;

    // Minimize an exclusive window that Windows left on screen after
    // Alt+Tab, see checkStrandedFullscreen.
    @Unique
    private static final long STRANDED_MILLIS = 500L;
    @Unique
    private static final int STRANDED_MAX_MINIMIZES = 3;
    @Unique
    private boolean borderlesswindow$watchingStranded;
    @Unique
    private long borderlesswindow$strandedSince;
    @Unique
    private int borderlesswindow$strandedMinimizesLeft;

    @Shadow
    @Final
    private long handle;

    @Shadow
    private boolean focused;

    @Shadow
    private boolean fullscreenRequested;

    @Shadow
    private boolean exclusiveFullscreen;

    @Shadow
    private boolean iconified;

    @Shadow
    private boolean fullscreen;

    @Shadow
    private int x;

    @Shadow
    private int y;

    @Shadow
    private int width;

    @Shadow
    private int height;

    @Shadow
    private int framebufferWidth;

    @Shadow
    private int framebufferHeight;

    @Shadow
    @Final
    private WindowEventHandler eventHandler;

    // The window was maximized when it left Windowed, see keepMaximized.
    @Unique
    private boolean borderlesswindow$wasMaximized;

    /**
     * A maximized window going fullscreen (Borderless or Fullscreen) and
     * back. Vanilla saves the window's position and size when it leaves
     * Windowed and restores them when it comes back, but not whether it
     * was maximized: it came back as a plain window the size of the
     * maximized one, slightly off the screen (a maximized window's frame
     * hangs past its edges), and that size stayed as the one to restore.
     * <p>
     * So on the way out a maximized window is restored first, and vanilla
     * saves its normal position and size; on the way back, once vanilla
     * has put those back, it is maximized again. Restore then returns it
     * to its normal size, as before going fullscreen. Costs one frame of
     * the normal window on the way out.
     */
    @Inject(method = "setMode", at = @At("HEAD"))
    private void borderlesswindow$saveMaximized(CallbackInfo ci) {
        if (!this.fullscreenRequested || this.fullscreen
                || (SDLVideo.SDL_GetWindowFlags(this.handle) & SDLVideo.SDL_WINDOW_MAXIMIZED) == 0L
                || !SDLVideo.SDL_RestoreWindow(this.handle)) {
            return;
        }
        SDLVideo.SDL_SyncWindow(this.handle);
        this.borderlesswindow$wasMaximized = true;
        // Read back now: the window's own move and resize events arrive
        // only after this frame, too late for the save that follows.
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer first = stack.mallocInt(1);
            IntBuffer second = stack.mallocInt(1);
            if (SDLVideo.SDL_GetWindowPosition(this.handle, first, second)) {
                this.x = first.get(0);
                this.y = second.get(0);
            }
            if (SDLVideo.SDL_GetWindowSize(this.handle, first, second)) {
                this.width = first.get(0);
                this.height = second.get(0);
            }
        }
    }

    /**
     * See saveMaximized. Also runs when entering fullscreen failed: vanilla
     * then turns the request back off, and the window is maximized again
     * as it was.
     */
    @Inject(method = "setMode", at = @At("TAIL"))
    private void borderlesswindow$keepMaximized(CallbackInfo ci) {
        if (this.borderlesswindow$wasMaximized && !this.fullscreenRequested) {
            this.borderlesswindow$wasMaximized = false;
            SDLVideo.SDL_MaximizeWindow(this.handle);
        }
    }

    /**
     * onFocus is where the window's focus events land (SDL's focus gained
     * and lost since 26.3, the GLFW focus callback before), so this fires
     * on every focus change (Minecraft.setWindowActive did this up to
     * 1.21.11; since 26.1 the window keeps the focus itself and
     * Minecraft.isWindowActive just asks it). Runs at TAIL: the window must
     * already hold the new value when the volume is recomputed. Clicking
     * back into the game also counts as activity for the idle cap.
     */
    @Inject(method = "onFocus", at = @At("TAIL"))
    private void borderlesswindow$onFocusChanged(boolean focused, CallbackInfo ci) {
        UnfocusedVolumeState.refresh();
        if (focused) {
            IdleFpsState.markInput();
            borderlesswindow$startFullscreenCheck();
            this.borderlesswindow$watchingStranded = false;
        } else {
            this.borderlesswindow$watchingStranded = true;
            this.borderlesswindow$strandedSince = 0L;
            this.borderlesswindow$strandedMinimizesLeft = STRANDED_MAX_MINIMIZES;
        }
    }

    /**
     * Back from Alt+Tab or the taskbar: SDL minimizes an exclusive
     * fullscreen window when it loses focus and gives the desktop its mode
     * back, then sets the window's mode again when it returns -- which
     * Windows can undo just as after a change (see checkFullscreenMode).
     * Watched both here and on focus: focus can come back while the window
     * still counts as minimized, which stops the watch at once. Only with
     * the focus: a window Windows put back on screen without it is not
     * really back (see checkStrandedFullscreen), and the watch starts from
     * onFocus once the focus does return.
     */
    @Inject(method = "onIconified", at = @At("TAIL"))
    private void borderlesswindow$onIconifiedChanged(boolean iconified, CallbackInfo ci) {
        if (!iconified && this.focused) {
            borderlesswindow$startFullscreenCheck();
        }
    }

    /**
     * Alt+Tab out of exclusive Fullscreen could leave the game on screen
     * (seen up to 26.2 with GLFW, on Windows 11 with NVIDIA and HDR, and in
     * vanilla as well): the window was minimized on focus loss and the
     * desktop got its mode back, but Windows showed the window again about
     * 100 ms later, without the focus, and its Fullscreen mode was set on
     * the monitor again. The game stayed in front, unfocused, until
     * clicked. It happened when the monitor had to change modes on the way
     * out (a Fullscreen Resolution other than the desktop's). Not seen yet
     * with SDL (nine quick Alt+Tabs on 26.3, OpenGL, all minimized and
     * stayed so); kept as a safeguard, since it only acts in that state.
     * <p>
     * SDL minimizes an exclusive window on focus loss (see
     * BorderlessHandler.initializeFromConfig), so one that has no focus
     * and is not minimized is never meant to last. Once that has held for
     * {@link #STRANDED_MILLIS} after the window lost focus, it is minimized
     * again, as SDL meant to. Only after a focus loss, at most a few times
     * per focus loss in case Windows keeps showing it again, and not if
     * another mod set SDL's hint to keep the window up. Windowed and
     * Borderless are never minimized. updateFullscreenIfChanged runs once
     * per frame.
     */
    @Inject(method = "updateFullscreenIfChanged", at = @At("TAIL"))
    private void borderlesswindow$checkStrandedFullscreen(CallbackInfo ci) {
        if (!this.borderlesswindow$watchingStranded) {
            return;
        }
        if (this.focused || !this.fullscreenRequested || !this.exclusiveFullscreen || this.iconified
                || (SDLVideo.SDL_GetWindowFlags(this.handle) & SDLVideo.SDL_WINDOW_FULLSCREEN) == 0L
                || !SDLHints.SDL_GetHintBoolean(SDLHints.SDL_HINT_VIDEO_MINIMIZE_ON_FOCUS_LOSS, true)) {
            this.borderlesswindow$strandedSince = 0L;
            return;
        }
        long now = Util.getMillis();
        if (this.borderlesswindow$strandedSince == 0L) {
            this.borderlesswindow$strandedSince = now;
            return;
        }
        if (now - this.borderlesswindow$strandedSince < STRANDED_MILLIS) {
            return;
        }
        this.borderlesswindow$strandedSince = 0L;
        if (this.borderlesswindow$strandedMinimizesLeft-- <= 0) {
            this.borderlesswindow$watchingStranded = false;
            return;
        }
        SDLVideo.SDL_MinimizeWindow(this.handle);
    }

    @Unique
    private void borderlesswindow$startFullscreenCheck() {
        this.borderlesswindow$checkDeadline = Util.getMillis() + FULLSCREEN_CHECK_MILLIS;
        this.borderlesswindow$retriesLeft = FULLSCREEN_MAX_RETRIES;
    }

    /**
     * Every time vanilla sets an exclusive Fullscreen mode -- entering
     * Fullscreen, or a new Fullscreen Resolution picked while in it -- the
     * next few seconds are watched over (see checkFullscreenMode).
     */
    @Inject(method = "applyExclusiveFullscreen", at = @At("RETURN"))
    private void borderlesswindow$watchFullscreenMode(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            borderlesswindow$startFullscreenCheck();
        }
    }

    /**
     * A new Fullscreen Resolution, picked while already in exclusive
     * Fullscreen: Windows (seen with NVIDIA) puts the desktop's mode back
     * 1 to 2 seconds after the game set it, while the window stays
     * exclusive. SDL took the mode without an error ("Exclusive target
     * ..." is logged, and the window still reports it as its fullscreen
     * mode), but the monitor is back at the desktop's resolution, even
     * after Alt+Tab. Entering Fullscreen from Windowed or Borderless, or at
     * startup, was never undone. The same happened with GLFW up to 26.2.
     * <p>
     * Checked every frame for a few seconds after the mode is set. If the
     * monitor no longer runs the window's fullscreen mode, the mode is
     * handed to SDL again, which sets it on the monitor once more; the
     * second call normally sticks. Stops early once the window leaves
     * exclusive Fullscreen or is minimized (SDL gives the desktop its mode
     * back on purpose then). updateFullscreenIfChanged runs once per frame,
     * before the frame is drawn.
     */
    // "resource": the SDL_DisplayMode structs SDL returns here point into
    // SDL's own memory (the window's mode, the display's current mode), so
    // they must never be closed: close() would free memory SDL owns. Only
    // structs the caller allocates are closed, as the stack copy below is.
    @SuppressWarnings("resource")
    @Inject(method = "updateFullscreenIfChanged", at = @At("TAIL"))
    private void borderlesswindow$checkFullscreenMode(CallbackInfo ci) {
        if (this.borderlesswindow$checkDeadline == 0L) {
            return;
        }
        if (!this.fullscreenRequested || !this.exclusiveFullscreen || this.iconified
                || (SDLVideo.SDL_GetWindowFlags(this.handle) & SDLVideo.SDL_WINDOW_FULLSCREEN) == 0L
                || Util.getMillis() > this.borderlesswindow$checkDeadline) {
            // Left Fullscreen, minimized or settled: stop watching.
            this.borderlesswindow$checkDeadline = 0L;
            return;
        }

        SDL_DisplayMode wanted = SDLVideo.SDL_GetWindowFullscreenMode(this.handle);
        int display = SDLVideo.SDL_GetDisplayForWindow(this.handle);
        SDL_DisplayMode actual = display == 0 ? null : SDLVideo.SDL_GetCurrentDisplayMode(display);
        if (wanted == null || actual == null
                || (actual.w() == wanted.w() && actual.h() == wanted.h()
                && actual.refresh_rate() == wanted.refresh_rate())) {
            return;
        }
        if (this.borderlesswindow$retriesLeft-- <= 0) {
            this.borderlesswindow$checkDeadline = 0L;
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // A copy: the struct SDL returned is the window's own, which
            // the call below overwrites.
            SDLVideo.SDL_SetWindowFullscreenMode(this.handle, SDL_DisplayMode.malloc(stack).set(wanted));
        }
    }

    /**
     * Fullscreen at a lower Fullscreen Resolution, straight to Borderless
     * (the selector, or vanilla's Exclusive Fullscreen toggle: both end in
     * setExclusiveFullscreen, which switches at once). Vanilla sizes the
     * Borderless window from the monitor's bounds, read while the window
     * still holds its exclusive mode, so before leaving it: the window came
     * out 721x480 in the corner of a 1920x1080 desktop. So the exclusive
     * mode is dropped first, which gives the monitor the desktop's mode
     * back while the window stays fullscreen (SDL's desktop fullscreen),
     * and the bounds vanilla reads next are the desktop's. Vanilla then
     * drops the same mode again and leaves fullscreen, as before.
     */
    @SuppressWarnings("resource") // SDL's own struct, never closed: see checkFullscreenMode
    @Inject(method = "applyBorderlessFullscreenWindow", at = @At("HEAD"))
    private void borderlesswindow$leaveExclusiveModeFirst(CallbackInfoReturnable<Boolean> cir) {
        if ((SDLVideo.SDL_GetWindowFlags(this.handle) & SDLVideo.SDL_WINDOW_FULLSCREEN) != 0L
                && SDLVideo.SDL_GetWindowFullscreenMode(this.handle) != null
                && SDLVideo.SDL_SetWindowFullscreenMode(this.handle, null)) {
            SDLVideo.SDL_SyncWindow(this.handle);
        }
    }

    /**
     * Keeps the game's size in step with an exclusive Fullscreen window.
     * After a new Fullscreen Resolution was applied from Sodium's page, the
     * window and the monitor ran the new mode (SDL reported its size, and
     * the swapchain was rebuilt at it), but the window's own size, and with
     * it the game's frame and GUI layout, stayed at the old one: no resize
     * event reached vanilla, and setMode had read the size back before the
     * change was done. The game kept drawing a 1920x1080 frame into a
     * 720x480 screen, showing only its lower-left corner.
     * <p>
     * So every frame in exclusive Fullscreen, the size SDL reports is
     * compared with the one vanilla holds, and on a difference handed over
     * as vanilla's own resize events would (onResize, onFramebufferResize):
     * the fields first, then framebufferSizeChanged, which reconfigures the
     * surface and lays the GUI out again (the main render target follows
     * the new size on the next frame, Minecraft.runTick). Two cheap SDL
     * queries a frame, and nothing at all outside exclusive Fullscreen,
     * where vanilla's events were never seen to miss. No borderless
     * padding here: it applies only to the Borderless window.
     */
    @Inject(method = "updateFullscreenIfChanged", at = @At("TAIL"))
    private void borderlesswindow$syncFullscreenSize(CallbackInfo ci) {
        if (!this.fullscreenRequested || !this.exclusiveFullscreen || this.iconified
                || (SDLVideo.SDL_GetWindowFlags(this.handle) & SDLVideo.SDL_WINDOW_FULLSCREEN) == 0L) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer first = stack.mallocInt(1);
            IntBuffer second = stack.mallocInt(1);
            if (SDLVideo.SDL_GetWindowSize(this.handle, first, second)) {
                this.width = first.get(0);
                this.height = second.get(0);
            }
            if (SDLVideo.SDL_GetWindowSizeInPixels(this.handle, first, second)
                    && first.get(0) > 0 && second.get(0) > 0
                    && (first.get(0) != this.framebufferWidth || second.get(0) != this.framebufferHeight)) {
                this.framebufferWidth = first.get(0);
                this.framebufferHeight = second.get(0);
                this.eventHandler.framebufferSizeChanged();
            }
        }
    }
}
