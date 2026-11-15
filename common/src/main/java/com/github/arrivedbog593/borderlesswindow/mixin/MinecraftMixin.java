package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.MinimizedRenderState;
import com.github.arrivedbog593.borderlesswindow.general.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.sodium.SodiumMenuRefresher;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes F11 follow the "F11 Mode" option, and skips drawing while the
 * window is minimized (see MinimizedRenderState).
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
     * F11 (the Toggle Fullscreen key, wherever it is bound) switches to the
     * mode the "F11 Mode" option picks: Windowed <-> Borderless, Windowed
     * <-> Fullscreen, or all 3 in turn. Vanilla's own toggle only flips
     * Fullscreen, keeping whichever kind Exclusive Fullscreen says.
     * <p>
     * Before the config is loaded (the first frame) the saved F11 Mode is
     * not known yet, so vanilla's toggle runs. Vanilla's video settings
     * screen follows through its tick (VideoSettingsScreenMixin), and the
     * Sodium page is refreshed here.
     */
    @Inject(method = "toggleFullscreen", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$f11(CallbackInfo ci) {
        if (!BorderlessHandler.isInitialized()) {
            return;
        }
        ci.cancel();
        BorderlessHandler.setMode(BorderlessHandler.getNextF11Mode());
        SodiumMenuRefresher.refreshIfSodiumMenuOpen();
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
                    target = "Lnet/minecraft/client/renderer/GameRenderer;render()V"))
    private boolean borderlesswindow$skipRenderWhenMinimized(GameRenderer renderer) {
        return !this.borderlesswindow$skipFrame;
    }
}
