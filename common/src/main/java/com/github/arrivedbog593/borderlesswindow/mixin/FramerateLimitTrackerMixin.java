package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.MinimizedRenderState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedFpsState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import net.minecraft.client.InactivityFpsLimit;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Caps the frame rate while the window is not focused (see
 * UnfocusedFpsState), after a stretch with no input (see IdleFpsState)
 * and while it is minimized (see MinimizedRenderState).
 * <p>
 * getFramerateLimit() is the single place runTick asks for the cap, right
 * after updateDisplay(): anything under 260 is handed to
 * RenderSystem.limitDisplayFPS, which sleeps the render thread in
 * glfwWaitEventsTimeout -- so input and focus events are still handled
 * while capped, and the cap lifts on the very next frame after the window
 * regains focus.
 * <p>
 * Vanilla's own throttling already lives in this method: 60 in menus
 * with no world loaded, 10 while minimized, and its Inactivity FPS Limit
 * option (30 after a minute without input, 10 after ten). Taking the
 * minimum keeps all of that, and keeps a regular Max Framerate that is
 * already lower than any of the caps. When several apply at once, the
 * lowest wins -- except vanilla's AFK mode, which the mod's idle caps
 * replace while they are on (see IdleFpsState).
 * <p>
 * Focus comes from Minecraft.windowActive, which vanilla updates from the
 * GLFW focus callback. Sodium does not touch this path, so the cap works
 * the same with and without it.
 */
@Mixin(FramerateLimitTracker.class)
public abstract class FramerateLimitTrackerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
    private void borderlesswindow$limitInBackground(CallbackInfoReturnable<Integer> cir) {
        int limit = cir.getReturnValueI();
        if (MinimizedRenderState.shouldSkipRendering()) {
            limit = Math.min(limit, MinimizedRenderState.MINIMIZED_FPS);
        }
        if (UnfocusedFpsState.isEnabled() && !this.minecraft.isWindowActive()) {
            limit = Math.min(limit, UnfocusedFpsState.getLimit());
        }
        limit = Math.min(limit, IdleFpsState.getCurrentCap());
        if (limit != cir.getReturnValueI()) {
            cir.setReturnValue(limit);
        }
    }

    /**
     * The option read at the top of getThrottleReason (its only
     * OptionInstance.get call). Reporting "Minimized" skips just the AFK
     * branch: the minimized check comes before it and the no-world-menu
     * check after it, both untouched. The option itself, its button and
     * options.txt keep the player's choice -- this only changes what the
     * tracker sees, and only while the mod's first idle stage is on.
     * isHeavilyThrottled reads the same method; vanilla uses it only to
     * silence the F3 pie profiler's slow-frame warnings.
     */
    @ModifyExpressionValue(method = "getThrottleReason",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    private Object borderlesswindow$replaceAfkMode(Object mode) {
        return IdleFpsState.replacesVanillaAfkMode() ? InactivityFpsLimit.MINIMIZED : mode;
    }
}
