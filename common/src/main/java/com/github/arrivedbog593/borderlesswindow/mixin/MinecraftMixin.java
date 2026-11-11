package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.MinimizedRenderState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Caps the frame rate while the window is not focused (see
 * UnfocusedFpsState) or after a stretch with no input (see IdleFpsState),
 * skips drawing while it is minimized (see
 * MinimizedRenderState), and re-applies the volume when the focus changes
 * (see UnfocusedVolumeState).
 * <p>
 * getFramerateLimit() is the single place runTick asks for the cap, right
 * after updateDisplay(): anything under 260 is handed to
 * RenderSystem.limitDisplayFPS, which sleeps the render thread in
 * glfwWaitEventsTimeout -- so input and focus events are still handled
 * while capped, and the cap lifts on the very next frame after the window
 * regains focus. Vanilla returns 60 here in menus with no world loaded;
 * taking the minimum keeps that, and keeps a regular Max Framerate that is
 * already lower than any of the caps. When several apply at once, the
 * lowest wins.
 * <p>
 * Focus comes from Minecraft.windowActive, which vanilla updates from the
 * GLFW focus callback. Sodium does not touch this path, so the cap works
 * the same with and without it.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public abstract boolean isWindowActive();

    @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
    private void borderlesswindow$limitInBackground(CallbackInfoReturnable<Integer> cir) {
        int limit = cir.getReturnValueI();
        if (MinimizedRenderState.shouldSkipRendering()) {
            limit = Math.min(limit, MinimizedRenderState.MINIMIZED_FPS);
        }
        if (UnfocusedFpsState.isEnabled() && !this.isWindowActive()) {
            limit = Math.min(limit, UnfocusedFpsState.getLimit());
        }
        limit = Math.min(limit, IdleFpsState.getCurrentCap());
        if (limit != cir.getReturnValueI()) {
            cir.setReturnValue(limit);
        }
    }

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
