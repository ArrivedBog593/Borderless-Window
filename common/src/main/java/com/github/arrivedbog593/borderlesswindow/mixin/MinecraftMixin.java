package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.MinimizedRenderState;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Skips drawing while the window is minimized (see MinimizedRenderState).
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
 * The decision is taken once, at extract, and reused for render and the
 * copy to the screen, so a window restored between the calls never gets
 * half a frame. That copy is skipped too: vanilla only skips it when its
 * last extracted state says minimized, and the last extracted state is
 * from before the skip began.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Unique
    private boolean borderlesswindow$skipFrame;

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

    @WrapWithCondition(method = "renderFrame",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen()V"))
    private boolean borderlesswindow$skipBlitWhenMinimized(RenderTarget target) {
        return !this.borderlesswindow$skipFrame;
    }
}
