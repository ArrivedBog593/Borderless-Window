package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.MinimizedRenderState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips drawing while the window is minimized (see MinimizedRenderState)
 * and re-applies the volume when the focus changes (see
 * UnfocusedVolumeState). The frame rate caps live in
 * FramerateLimitTrackerMixin.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    /**
     * The whole frame -- world, GUI, screens -- is drawn by this one call,
     * between NeoForge's RenderFrameEvent Pre and Post. Everything else in
     * runTick (ticks, packets, sound) still runs while it is skipped.
     */
    @WrapWithCondition(method = "runTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private boolean borderlesswindow$skipRenderWhenMinimized(GameRenderer renderer,
                                                             DeltaTracker deltaTracker, boolean renderLevel) {
        return !MinimizedRenderState.shouldSkipRendering();
    }

    /**
     * setWindowActive is the GLFW focus callback's landing point, so this
     * fires on every focus change. Runs at TAIL: windowActive must already
     * hold the new value when the listener gain is recomputed. Clicking
     * back into the game also counts as activity for the idle cap.
     */
    @Inject(method = "setWindowActive", at = @At("TAIL"))
    private void borderlesswindow$onFocusChanged(boolean focused, CallbackInfo ci) {
        UnfocusedVolumeState.refresh();
        if (focused) {
            IdleFpsState.markInput();
        }
    }
}
