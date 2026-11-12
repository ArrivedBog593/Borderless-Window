package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.MinimizedRenderState;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Makes the Fullscreen screen mode always exclusive, and skips drawing
 * while the window is minimized (see MinimizedRenderState).
 * The frame rate caps live in FramerateLimitTrackerMixin; the volume's
 * reaction to focus changes, in WindowMixin.
 * <p>
 * Since 26.1 renderFrame builds each frame in three steps: update (camera
 * and chunk updates), extract (copies the world and the GUI into the
 * frame's render state) and render (draws that state). Extract and render
 * are skipped together: render is what empties what extract gathered
 * (submitNodeStorage, guiRenderer and others end their frame there), so
 * extracting without rendering would pile up a frame's worth of state
 * every frame. Everything else in runTick (ticks, packets, sound) and
 * update still run.
 * <p>
 * The decision is taken once, at extract, and reused for render, so a
 * window restored between the calls never gets half a frame. The copy to
 * the screen is left to vanilla: since 26.2 it only takes an image from
 * the window surface while the window is not minimized (a 0x0
 * framebuffer, which is what minimizing gives on Windows), and without
 * one it neither copies nor presents. Wherever a minimized window keeps
 * its size, the last frame drawn is presented again, which costs a copy.
 * Skipping the copy or the present ourselves would break the frame: an
 * image taken and never presented stops every later frame.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Unique
    private boolean borderlesswindow$skipFrame;

    /**
     * Since 26.1 Minecraft has an Exclusive Fullscreen option, off by
     * default. Its main effect is one GLFW hint, GLFW_SOFT_FULLSCREEN, set
     * right before the window is created (in GLX._initGlfw up to 26.1.2,
     * here in the constructor since 26.2). That hint exists only in LWJGL's
     * own GLFW fork, and on Windows all it does is make a fullscreen window
     * 1 px taller than the monitor -- the same trick BorderlessHandler uses
     * for Borderless -- so the driver does not take it over as exclusive.
     * The window is still on top of everything and still minimizes when it
     * loses focus: a worse Borderless. Sodium turns the option on at first
     * launch (except with a Chinese, Japanese or Korean input method), so
     * without this the same Fullscreen mode would behave differently with
     * and without Sodium.
     * <p>
     * The fork keeps the hint library-wide and reads it each time it fits a
     * window to a monitor, not only when one is created, so setting it here
     * covers every switch to Fullscreen. The option's other effect, since
     * 26.2, is the window's own copy of it, which takes 1 px off a soft
     * fullscreen's framebuffer; WindowMixin.isSoftScreen ties that to the
     * Borderless mode instead. The vanilla option is hidden from the video
     * settings (VideoSettingsScreenMixin); the Screen Mode selector already
     * offers both kinds of fullscreen.
     * <p>
     * The only glfwWindowHint call in the constructor. It runs once per
     * graphics backend tried (Vulkan falling back to OpenGL, or the other
     * way around), and this covers each.
     */
    @ModifyArg(method = "<init>",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V"),
            index = 1)
    private int borderlesswindow$exclusiveFullscreen(int softFullscreen) {
        return GLFW.GLFW_FALSE;
    }

    @WrapWithCondition(method = "renderFrame",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;extract(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private boolean borderlesswindow$skipExtractWhenMinimized(GameRenderer renderer,
                                                              DeltaTracker deltaTracker, boolean advanceGameTime) {
        this.borderlesswindow$skipFrame = MinimizedRenderState.shouldSkipRendering();
        return !this.borderlesswindow$skipFrame;
    }

    @WrapWithCondition(method = "renderFrame",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private boolean borderlesswindow$skipRenderWhenMinimized(GameRenderer renderer,
                                                             DeltaTracker deltaTracker, boolean advanceGameTime) {
        return !this.borderlesswindow$skipFrame;
    }
}
